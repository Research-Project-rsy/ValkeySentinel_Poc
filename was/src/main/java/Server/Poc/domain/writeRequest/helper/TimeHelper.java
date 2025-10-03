package Server.Poc.domain.writeRequest.helper;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeHelper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM-dd-HH-mm-ss");

    public static String getCurrentFormattedTime() {
        return LocalDateTime.now().format(FORMATTER);
    }
}