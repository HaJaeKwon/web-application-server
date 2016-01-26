# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* 

클라이언트의 요청에 대한 사항은 (어디로 접근할지가 오겟지.) InputStream in 에 모두 담겨있다.
여기 담긴 정보를 파싱하는 것이 필요하다.

BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = br.readLine(); // 한줄씩읽는다. 필요하다면 반복문을 통해 계속 읽는다.
			String[] tokens = line.split(" ");
			
여기서 tokens에 담긴 정보들중 /index.html 을 url에 옮겨 담는다.
      url = tokens[1];
      body = Files.readAllBytes(new File("./webapp" + url).toPath());
      				
그리고 이 body를 DataOutputStream dos 에 담아 flush로 보내면 된다.

### 요구사항 2 - get 방식으로 회원가입
* 
회원가입을 하게되면 create? ~~ 로 클라이언트에서 요청이 오는데 이것을 파싱하여 userId, password, name, email로 분리한다.
분리하는 과정은 parseQueryString 를 사용하여 한다.
이 함수에는 
return Arrays.stream(tokens)
	.map(t -> getKeyValue(t, "="))
	.filter(p -> p != null)
	.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
부분이 있는데 아마 자바8에서 부터 쓰이는 함수적 표현? 인 것 같다.
주로 data를 파싱할때, 어떤 stream에서 정보를 빼와 재조합 하는데 쓰이는 방식 같으며 굉장히 편리해보인다.

파싱이 끝나서 Map이 리턴되면 저장된 value값들을 user객체에 옮겨 담고 이것을 List에 add 하면 되겠다.

### 요구사항 3 - post 방식으로 회원가입
* 
GET 과의 가장 큰 차이점은 회원가입한 정보가 hader가 아니라 body에 온다는 것이다.
처음에 해맸던 이유는 br.readline이 빈줄을 읽지 못하기 때문에
br로 header끝까지 읽고 그 다음 br로 한 줄 더 읽으면 body 부분을 읽을 것이라고 생각했기 때문이다.
하지만 이렇게는 읽지 못했고 content-length를 정확히 읽어서 그 다음부분에서 그 만큼만 정확히 읽는 방법이 필요했다.
(여기서는 IOUtils의 readData를 사용했다.)
가입 정보를 읽은 순간부터는 GET방식과 동일했다.

### 요구사항 4 - redirect 방식으로 이동
* 

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 
