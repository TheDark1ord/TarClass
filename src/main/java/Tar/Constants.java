package Tar;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Constants {
    public static final int mb = 1_048_576;
    public static final int max_buffer_size = 10 * mb;
    public static final Charset headerEncoding = StandardCharsets.UTF_8;
}
