import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scurl {


    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));

        if (args.length < 1) {
            System.out.println("Usage: scurl [option] url");
            System.out.println("Options:");
            System.out.println("-v                  verbose, 요청, 응답 헤더를 출력한다.");
            System.out.println("-H <line>           임의의 헤더를 서버로 전송한다.");
            System.out.println("-d <data>           POST, PUT 등에 데이터를 전송한다.");
            System.out.println("-X <command>        사용할 method를 지정한다. 지정되지 않은 경우, 기본값은 GET");
            System.out.println("-L                  서버의 응답이 30x 계열이면 다음 응답을 따라 간다.");
            System.out.println("-F <name=content>   multipart/form-data를 구성하여 전송한다. content 부분에 @filename을 사용할 수 있다.");
            System.exit(1);
        }

        String[] urlSplit = args[args.length-1].replace("http://", "").split("/");
        String host = urlSplit[0];
        String path = "/" + urlSplit[1];
        System.out.println(host);

        String method = "GET";
        Boolean isVerbose = false;
        List<String> headerList = new ArrayList<>();
        List<String> dataList = new ArrayList<>();
        final List<String> optionList = new ArrayList<>();
        optionList.add("-v");
        optionList.add("-H");
        optionList.add("-d");
        optionList.add("-X");
        optionList.add("-L");
        optionList.add("-F");


        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-v")) {
                isVerbose = true;
            }

            if (args[i].equals("-X")) {
                method = args[++i];
            }

            if (args[i].equals("-H")) {
                // 다시 되돌아가면 의미없는 반복 횟수가 그만큼 더 늘어나므로 조정해주는 인덱스
                int leapidx = 0;

                for (int j = i + 1; j < args.length - 1; j++) {
                    if (optionList.contains(args[j])) {
                        break;
                    }

                    headerList.add(args[j]);
                    leapidx = j;
                }
                
                i = leapidx;
            }

            if (args[i].equals("-d")) {
                method = "POST";

                int leapidx = 0;

                for (int j = i + 1; j < args.length - 1; j+=2) {
                    if (optionList.contains(args[j])) {
                        break;
                    }

                    dataList.add(args[j].concat(args[j+1]));
                    leapidx = j;
                }
                
                i = leapidx;
            }
        }

        try (Socket clientSocket = new Socket(host, 80);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            ) {
            
            if (isVerbose) {
                System.out.println("* Trying " + clientSocket.getInetAddress().getHostAddress() + "...");
                System.out.println("* Connected to httpbin.org (" + clientSocket.getInetAddress().getHostAddress() + ") port 80 (#0)");
            }
            
                
            // HTTP 요청 메시지 설정
            StringBuilder requestBuilder = new StringBuilder();
            requestBuilder.append(method).append(" ").append(path).append(" HTTP/1.1\r\n");

            requestBuilder.append("Host: ").append(host).append("\r\n");
            requestBuilder.append("User-Agent: curl/7.79.1\r\n");
            requestBuilder.append("Accept: */*\r\n");
            requestBuilder.append("Connection: close\r\n");

            // -H 옵션 추가한 경우
            for (String string : headerList) {
                requestBuilder.append(string + "\r\n");
            }

            if (!dataList.isEmpty()) {
                int totalLength = 0;

                // 전송 데이터 길이
                for (String data : dataList) {
                    totalLength += data.length();
                }

                requestBuilder.append("Content-Length: ").append(totalLength).append("\r\n");
            }

            requestBuilder.append("\r\n");

            // request Body
            // -d 옵션 추가한 경우
            if (!dataList.isEmpty()) {
                // 데이터 전송
                for (int i = 0; i < dataList.size(); i++) {
                    requestBuilder.append(dataList.get(i));
                    
                    if (i != dataList.size() - 1) {
                        requestBuilder.append(", ");
                    }
                }
                // requestBuilder.append("{\"hello\": \"world\"}").append("\r\n");
            }

            if (isVerbose) {
                String[] split = requestBuilder.toString().split("\r\n");
                for (int i = 0; i < split.length; i++) {
                    System.out.println("> " + split[i]);
                }
            }

            // 요청 메시지를 소켓을 통해 전송
            bufferedWriter.write(requestBuilder.toString());
            bufferedWriter.flush();


            String line;
            // connection이 closed 상태가 아니라면
            // 읽어올 때의 end Of File이 아니라서 읽어올 수 없어 무한 루프가 발생
            Boolean isHeader = true;
            while ((line = bufferedReader.readLine()) != null) {
                if (isHeader) {
                    // 빈공백을 기준으로 요청과 응답 메시지 구분
                    if (line.isEmpty()) { 
                        isHeader = false;
                    }
                    
                    if (isVerbose) {
                        System.out.println("< " + line);
                    }
                } else {
                    System.out.println(line);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
