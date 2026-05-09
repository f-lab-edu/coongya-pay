# Spring Bean Validation

## 1. `@Valid` 검증 원리
1. HTTP 요청(JSON)
2. Jackson이 JSON -> DTO 객체로 역직렬화
   - `@RequestBody`가 붙은 파라미터는 스프링이 `HttpMessageConverter`로 JSON을 자바 객체로 변환한다.
3. `@Valid` 존재 시 스프링이 검증 트리거
   - Spring MVC의 `HandlerMethodArgumentResolver`가 `@Valid`를 보면 자동으로 Validator를 호출한다. 이 Validator는 Spring boot의 기본 의존성에 포함된 Hibernate Validator(Jakarta Bean Validation의 구현체)이다.  
4. Hibernate Validator가 어노테이션을 하나씩 평가
   - Hibernate Validator가 객체의 모든 필드를 순회하면서 어노테이션마다 `ConstraintValidator`를 실행한다. 

    | 어노테이션 | 검증자 클래스(Hibernate Validator 내부) | 
    |---|---|
    | @NotBlank | NotBlankValidator |
    | @Email | EmailValidator |
    | @Size | SizeValidator | 
    | @Pattern | PatternValidator |
    - 각 검증자가 boolean을 반환한다. true면 검증 통과, false면 위반이므로 `ConstraintViolation`으로 기록한다.
5. 검증 위반 발생 시 `MethodArgumentNotValidException`을 던짐
   - 모든 위반이 모이면 Spring MVC가 다음 예외를 던진다.
   - ```throw new MethodArgumentNotValidException(parameter, bindingResult);```
   - `bindingResult.getFieldErrors()`: 위반 목록
   - 각 FieldError는 field(필드명), code(어노테이션 이름), defaultMessage를 가진다.
6. `GlobalExceptionHandler`가 예외를 잡아서 응답 생성

## 2. 필드 검증 상세
- 예시: [SignupRequest.java](../src/main/java/com/flab/coongyapay/user/controller/dto/SignupRequest.java)

### 2.1. email
```
@NotBlank(message = "FIELD_REQUIRED")
@Email(message = "INVALID_EMAIL_FORMAT")
private String email;
```
- `@NotBlank`
  - null, 빈 문자열, 공백만 있는 문자열 거부
  - 통과 조건: 길이 >= 1 & 공백 외 문자 1개 이상
- `@Email`
  - Hibernate Validator가 RFC 5321 기반 정규식으로 형식 검사
  - null은 통과되므로 `@NotBlank`와 함께 써야 한다.
  - 다소 느슨한 검사이므로 엄격하게 검증하려면 `@Pattern`을 추가해야 한다.
    - (예시) `user@test`도 통과

### 2.2. password
```
@NotBlank(message = "FIELD_REQUIRED")
@Size(min=8, max=32, message = "INVALID_PASSWORD_LENGTH")
@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "INVALID_PASSWORD_FORMAT")
private String password;
```
- `@Size`
  - 문자열 길이 검사
  - min <= length <= max
- `@Pattern`
  - ^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$ 정규식 분해

  | 부분 | 의미 |
  |---|---|
  | ^ | 문자열 시작 |
  | (?=.*[A-Za-z]) | lookahead: 어딘가에 영문자 1개 이상 있는지 검사 |
  | (?=.*\\d) | lookahead: 어딘가에 숫자 1개 이상 있는지 검사 |
  | (?=.*[^A-Za-z0-9]) | lookahead: 어딘가에 영문/숫자가 아닌 문자, 즉, 특수문자가 1개 이상 있는지 검사 |
  | .+ | 1자 이상 (실제 매칭) |
  | $ | 문자열 끝 |

  - Lookahead (?=...)
    - 위치만 확인하고, 문자를 소비(consume)하지 않는다.
    - 한 번에 여러 조건을 AND 검사할 때 사용한다.
    - 위의 예시에서는 "영문이 있고, 숫자가 있고, 특수문자도 있는지" 세 조건을 동시에 검증한다.

### 2.3. name
```
@NotBlank(message = "FIELD_REQUIRED")
@Size(min=1, max=30, message = "INVALID_NAME_LENGTH")
@Pattern(regexp = "^([가-힣]+|[A-Za-z ]+)$", message = "INVALID_NAME_FORMAT")
private String name;
```
- `@Pattern`
  - ^([가-힣]+|[A-Za-z ]+)$ 정규식 분해

  | 부분     | 의미 |
  |--------|---|
  | ^...$  | 문자열 전체 매칭 |
  | [가-힣]+ | 가 ~ 힣 유니코드 범위 내의 한글 음절 1개 이상 있는지 검사 |
  | \|     | OR: 둘 중 하나만 매칭 |
  | [A-Za-z ]+ | 영문 대/소문자 또는 공백 1개 이상 있는지 검사 |

  - [가-힣]은 한글 완성형 음절 (유니코드 U+AC00 ~ U+D7A3)만 통과, ㄱ, ㅏ와 같은 자모 단독은 통과 안 됨
  - |은 Alternation의 의미로 둘 중 하나만 허용되고 혼용 불가하다. 
    - (예시) Kim쿵야 통과 안 됨

### 2.4. transferPin
```
@NotBlank(message = "FIELD_REQUIRED")
@Pattern(regexp = "^\\d{6}$", message = "INVALID_TRANSFER_PIN_FORMAT")
private String transferPin;
```
- `@Pattern`
  - ^\\d{6}$ 정규식 분해
  
  | 부분 | 의미 |
  |---|---|
  | ^...$ | 문자열 전체 매칭 |
  | \\d | 숫자 한 글자 ([0-9]와 동일한 의미) |
  | {6} | 정확히 6번 반복 |

  - 정확히 숫자 6자리로만 구성되었는지 검증한다.
  - leading zero 보존된다. 
    - (예시) "012345"도 통과 

## 3. 어노테이션 순서와 검증 우선순위
```
@NotBlank(message = "FIELD_REQUIRED")
@Size(min=8, max=32, message = "INVALID_PASSWORD_LENGTH")
@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "INVALID_PASSWORD_FORMAT")
private String password;
```
Bean Validation은 여러 어노테이션을 모두 검사하고 결과를 모은다.  
어노테이션 작성 순서가 검증 순서와 일치한다는 보장이 없다.  
(예시) 빈 문자열 "" 입력 시
- @NotBlank -> "FIELD_REQUIRED"
- @Size -> "INVALID_PASSWORD_LENGTH"
- @Pattern -> "INVALID_PASSWORD_FORMAT"  

세 위반이 모두 발생한다.  
검증 우선순위를 엄격하게 관리해야 할 경우에는 `@Valid`가 아닌 `@Validated`를 쓰고 `@GroupSequence`를 도입하여 명시적으로 지정 가능하다.