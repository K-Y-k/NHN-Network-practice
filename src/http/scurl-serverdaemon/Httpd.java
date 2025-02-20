import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Httpd {
    public static void main(String[] args) {
        int port = 9999;

        // 서버 소켓 생성
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("서버가 " + port + " 포트에서 실행 중...");

            while (true) {
                // 쓰레드에서 소켓을 사용할 때까지 자동으로 닫히지 않도록 try-with-resource로 하면 안된다.
                // 대신, 소켓을 사용한 후 명시적으로 닫아야 한다.
                Socket client = serverSocket.accept();
                System.out.println("클라이언트 연결됨: " + client.getPort());

                try {
                    // 작업할 쓰레드 생성
                    // 여기서는 람다식을 적용한 익명 클래스로 만듬
                    Thread thread = new Thread(() -> {
                        /**
                         * 입출력 클래스를 활용하여 
                         * 클라이언트로 연결한 소켓을 통해
                         * 요청 메시지와 응답 메시지를 주고 받는 작업 로직
                         */
                        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));) {
                            

                            String requestLine = bufferedReader.readLine();
                            if (requestLine == null) {
                                return;
                            }
    
                            String[] requestParts = requestLine.split(" ");
                            String method = requestParts[0]; // http 메소드
                            String path = requestParts[1];   // /file-path
                            System.out.println(method);
                            System.out.println(path);
    
    
                            // 상위 디렉토리 요청 방지
                            if (path.contains("..")) {
                                System.out.println("상위 디렉토리로 이동할 수 없습니다.");
                                sendResponse(bufferedWriter, "403 Forbidden", 403);
                                return;
                            }

    
                            // 요청된 파일의 실제 경로 설정
                            // System.getProperty("user.dir")는 현재 프로그램이 실행되고 있는 디렉토리 경로로 루트 디렉토리가 된다.
                            Path filePath = Paths.get(System.getProperty("user.dir"), path);
                            System.out.println(filePath);
                            System.out.println(method);
    

                            // Get 요청인 경우
                            if (method.equals("GET")) {
                                // "/file-path" 요청 처리
    
                                // "/"인 경우
                                if (path.equals("/")) {
                                    // 루트 디렉토리의 파일들을 반환
                                    System.out.println("웹 브라우저에 현재 디렉토리의 파일들을 출력합니다.");
                                    listDirectory(filePath, bufferedWriter);
                                } else {
                                    // "/파일명" 의 경로인 경우
    
                                    // 해당 경로의 파일이 존재하면
                                    if (Files.exists(filePath)) {
                                        // 읽을 수 있는 파일이면
                                        if (Files.isReadable(filePath)) {
                                            System.out.println("파일을 읽고 응답합니다.");
    
                                            // 해당 파일의 내용을 모두 바이트 단위로 읽고
                                            byte[] fileContent = Files.readAllBytes(filePath);
    
                                            // String 형으로 생성해서 전달
                                            sendResponse(bufferedWriter, new String(fileContent), 200);
                                        } else {
                                            // 읽을 수 없는 파일
                                            System.out.println("파일을 읽을 수 없습니다.");
    
                                            sendResponse(bufferedWriter, "403 Forbidden", 403);
                                        }
                                    } else {
                                        System.out.println("해당 파일이 존재하지 않습니다.");
                                        sendResponse(bufferedWriter, "404 Not Found", 404);
                                    }
                                }
                            }
    
                            // DELETE 요청인 경우
                            if (method.equals("DELETE")) {
                                // 해당 경로의 파일 클래스 생성
                                File findFile = filePath.toFile();
    
                                // 파일이 존재하는지 검증
                                if (!findFile.exists()) {
                                    System.out.println(findFile.getName() + " 파일이 존재하지 않습니다.");
                                    sendResponse(bufferedWriter, "404 Not Found", 404);
                                } else {
                                    // 파일이 존재하면 삭제하고 성공 검증
                                    if (findFile.delete()) {
                                        System.out.println(findFile.getName() + " 파일이 성공적으로 삭제되었습니다.");
                                        sendResponse(bufferedWriter, findFile.getName() + "삭제 성공", 200);
                                    } else {
                                        System.out.println(findFile.getName() + " 파일 삭제 권한이 없어 실패했습니다.");
                                        sendResponse(bufferedWriter, "403 Forbidden", 403);
                                    }
                                }
                            }
    
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                client.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    // 쓰레드 시작
                    thread.start();
                } catch (Exception e) {
                    throw new RuntimeException("socket, buffer try 실패 : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("serverSocket try 실패 : " + e.getMessage());
        }
    }
    

     /**
      * 현재 디렉토리의 파일 목록을 HTML 형식으로 응답 메소드
      */
    private static void listDirectory(Path dirPath, BufferedWriter bufferedWriter) throws IOException {
        // 해당 디렉토리가 존재하지 않으면
        if (!Files.isDirectory(dirPath)) {
            sendResponse(bufferedWriter, "404 Not Found", 404);
            return;
        }


        // HTML 가공
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><body><h1>Directory List</h1><ul>");

        // 해당 디렉토리의 파일들을 HTML로 나열
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path entry : stream) {
                htmlContent.append("<li><a href=\"").append(entry.getFileName()).append("\">")
                        .append(entry.getFileName())
                        .append("</a></li>");
            }
        }

        htmlContent.append("</ul></body></html>");
        

        // 응답 메시지 전송
        bufferedWriter.write("HTTP/1.1 " + 200 + " " + getStatusMessage(200) + "\r\n");
        bufferedWriter.write("Content-Type: text/html; charset=UTF-8\r\n");
        bufferedWriter.write("Content-Length: " + htmlContent.length() + "\r\n");
        bufferedWriter.write("Content-Length: " + htmlContent.length() + "\r\n");
        bufferedWriter.write("\r\n");

        bufferedWriter.write(htmlContent.toString());
        bufferedWriter.flush();
    }


    /**
     * 응답 메시지로 가공해서 전송 메소드
     */
    private static void sendResponse(BufferedWriter bufferedWriter, String content, int statusCode) throws IOException {
        bufferedWriter.write("HTTP/1.1 " + statusCode + " " + getStatusMessage(statusCode) + "\r\n");
        bufferedWriter.write("Content-Type: text/html; charset=UTF-8\r\n");
        bufferedWriter.write("Content-Length: " + content.length() + "\r\n");
        bufferedWriter.write("Connection: close\r\n");
        bufferedWriter.write("\r\n");

        bufferedWriter.write(content);
        bufferedWriter.flush();
    }

    
    private static String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 404: return "Not Found";
            case 403: return "Forbidden";
            default: return "Unknown";
        }
    }

    private static String getContentType(Path filePath) {
        String filename = filePath.toString().toLowerCase();
        
        if (filename.endsWith(".html")) {
            return "text/html";
        } else if (filename.endsWith(".txt")) {
            return "text/plain";
        } else if (filename.endsWith(".jpg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }
}
