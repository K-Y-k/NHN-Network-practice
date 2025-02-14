import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeResponse implements Response {

    private final static String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public String getMethod() {
        return "time";
    }

    @Override
    public String execute(String value) {
        // LocalDateTime을 이용해서 현재 시간을 설정하세요.
        LocalDateTime now = LocalDateTime.now();

        // value 형식이 "" or null 이면 현재 시간을 DEFAULT_DATETIME_FORMAT 으로 반환합니다.
        if (StringUtils.isEmpty(value)) {
            DateTimeFormatter nomalFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);
            return now.format(nomalFormatter);
        }

        // 현재 시간을 value 형식으로 formatting 하는 과정에서 value 형식이 잘못 되었다면 
        // 현재 시간을 DEFAULT_DATETIME_FORMAT 으로 반환합니다.
        try {
            DateTimeFormatter valueFormatter = DateTimeFormatter.ofPattern(value);
            return now.format(valueFormatter);
        } catch (Exception e) {
            DateTimeFormatter nomalFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);
            return now.format(nomalFormatter);
        }
    }
}