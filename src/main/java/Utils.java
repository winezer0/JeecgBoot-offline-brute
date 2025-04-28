import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;

public class Utils {
    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param src 字节数组
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 从 JSON 文件中加载用户数据
     *
     * @param jsonFilePath JSON 文件路径
     * @return 包含 username、password 和 salt 的用户数据列表
     * @throws IOException 如果文件读取失败
     */
    public static List<Map<String, String>> loadUserDataFromJson(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Path path = Paths.get(jsonFilePath);

        // 检查文件是否存在
        if (!Files.exists(path)) {
            throw new IOException("JSON 文件不存在: " + jsonFilePath);
        }

        // 读取 JSON 数据并解析为 Map 列表
        TypeReference<List<Map<String, String>>> typeRef = new TypeReference<List<Map<String, String>>>() {};
        List<Map<String, String>> rawData = objectMapper.readValue(Files.readAllBytes(path), typeRef);
        return rawData;
    }
}
