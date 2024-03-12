
### API 가이드
[카카오페이 문서발송 단건](./document/카카오페이내문서함_1.문서발송(단건).pdf)   
[카카오페이 문서발송 대량](./document/카카오페이내문서함_1.문서발송(대량).pdf)   
[카카오페이 문서발송 네트워크가이드](./document/카카오페이내문서함_1.네트워크가이드.pdf)

### swagger
[API URL](http://localhost:8081/swagger-ui.html)   
[Front URL](http://localhost:8080/swagger-ui.html)   
[Front test page](http://localhost:8080/api/kakaopay/test)

### API 결과 수신
* 정상 수신
```java
public class ApiResponseDTO<T> implements Serializable {
    private static final String FAIL_STATUS = "fail";
    private static final String ERROR_STATUS = "error";

    @Schema(example = "true", description = "에러인 경우 false", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean success;

    @Schema(example = " ", description = "HttpStatus.OK", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "결과 데이타, 오류시 null", example = " ")
    private T data;

    @Schema(description = "오류 발생시 오류 메세지", example = " ", requiredMode = Schema.RequiredMode.AUTO)
    @Setter
    private String message;

    @Schema(example = " ", description = "HttpStatus.OK", requiredMode = Schema.RequiredMode.AUTO)
    private HttpStatus httpStatus;

    @Schema(description = "API 실행 결과 데이타 수")
    private int count;
}
```
* 정상 수신
```json
{
  "success": true,
  "code": "200",
  "httpStatus": "OK",
  "message": "성공했습니다.",
  "data": {
    "token_status": "USED",
    "token_expires_at": 1624344762,
    "token_used_at": 0,
    "doc_box_sent_at": 0,
    "doc_box_received_at": 0,
    "authenticated_at": 0,
    "user_notified_at": 0,
    "payload": "payload 파라미터 입니다.",
    "signed_at": 0
  },
  "count": 1,
  "paginationInfo": null
}
```
* 에러 수신
```json
{
  "success": false,
  "code": "error",
  "data": null,
  "message": "로그인 정보가 올바르지 않습니다.",
  "httpStatus": "BAD_REQUEST",
  "count": 0,
  "paginationInfo": null
}
```    
* API 호출 결과가 서버등(네트웍장애)의 장애인 경우를 제외 하고   
  예외로 return 되는 경우는 없다(발생시 공통팀에 반드시 알려 줄 것) 
```js
    $.ajax({
        url: url,
        type: method,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        data: JSON.stringify(data),
        beforeSend: (xhr) => {
            //xhr.setRequestHeader(header, token);
            $("#loading").show();

        },
        success: function (res, textStatus) {
            console.log( JSON.stringify(res));
            if(res.success){
                //정상 응답
                $("#resData").text(res.data)
            }else{
                //에러 응답
                $("#errData").text(JSON.stringify(res));
            }
        },
        error : function(data) {
            // 여기로 오는 경우 공통팀에 알려 주세요
            alert("점검필요-error로 return", data.responseText);
        },
        complete: () => {
            $("#loading").hide();
        }
    });
```
### API(Restful call) validation
* Controller 단에서 @Validated 사용으로 처리 가능
* But, 이경우 API 로그를 남기기 위해 Service 단에서 체크 하도록 컨트롤러 단에서는 유효성 체크 skip

### spring validation
```text
@Valid는 Java, @Validated는 Spring에서 지원하는 어노테이션
@Validated는 @Valid의 기능을 포함하고, 유효성을 검토할 그룹을 지정할 수 있는 기능이 추가됨
```

```java
@Null       // null만 혀용
@NotNull    // null을 허용하지 않습니다. "", " "는 허용
@NotEmpty   // null, ""을 허용하지 않습니다. " "는 허용
@NotBlank   // null, "", " " 모두 허용하지 않습니다.

@Email              // 이메일 형식을 검사합니다. 다만 ""의 경우를 통과 시킵니다
@Pattern(regexp = ) // 정규식을 검사할 때 사용됩니다.
@Size(min=, max=)   // 길이를 제한할 때 사용됩니다.

@Max(value = )      // value 이하의 값을 받을 때 사용됩니다.
@Min(value = )      // value 이상의 값을 받을 때 사용됩니다.

@Positive           // 값을 양수로 제한합니다.
@PositiveOrZero     // 값을 양수와 0만 가능하도록 제한합니다.

@Negative           // 값을 음수로 제한합니다.
@NegativeOrZero     // 값을 음수와 0만 가능하도록 제한합니다.

@Future         // 현재보다 미래
@Past           // 현재보다 과거

@AssertFalse    // false 여부, null은 체크하지 않습니다.
@AssertTrue     // true 여부, null은 체크하지 않습니다.
```
### intellij devtools 활성
```text
1. IntelliJ - Preferencs… 
2. 컴파일러 - build project automatically(프로젝트 자동 빌드) 체크
3. Advanced Settings > Compiler 
   Allow auto-make to start even if developed application is current running
   (개발된 애플리케이션이 현재 실행 중인 경우에도 auto-make가 시작되도록 허용) 체크
# 1 ~ 3항 까지 설정후 에도 안되는 경우만 4번 설정
4. 서버설정 : Edit Configurations...
   Modfy Options > On Update Action > Update Resources 

```
### ens-api 배포 및 run : profile에 따라 local|dev|prod
```shell
# jdk : azul-17.0.1
# 프로젝트 root 폴더로 이동 : ens-parent
# 패키지 생성 : local|dev|prod
$ mvnw clean package -P local

# 실행 : 프로젝트폴더/ens-parent/ens-api/target에 생성된 jar파일 실행
$ c:\tools\java\azul-17.0.1\java -jar -Dspring.profiles.active=local .\mens-api.jar

# mvn 명령어 설명 
# -pl [모듈명] : 모듈명의 프로젝트만 빌드
# -am : 의존성 있는 프로젝트 함께 빌드 - C가 A를 디펜던시로 가지고 있으며 C를 빌드하면 A -> C 순으로 빌드
$ mvnw clean package -pl mens-api -am -P local
# -amd : 의존성 있는 타 프로젝트 빌드 - C가 A를 디펜던시로 가지고 있는 경우 A를 빌드 하면 A -> C 순으로 빌드
$ mvnw clean package -pl mens-core -amd -P local

mvn clean package -pl mens-batch -am -P prod

```
### 스프링 배치 DB schema
[mysql DDL 스크립트](./document/batch-schema-mysql.sql)
