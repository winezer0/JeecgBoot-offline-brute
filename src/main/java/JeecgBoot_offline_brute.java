import java.io.IOException;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.PBEParameterSpec;
import java.util.*;
import java.nio.file.*;
import org.apache.commons.cli.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JeecgBoot_offline_brute {
    private static Options initOptions() {
        // 定义选项
        Options options = new Options();

        Option jsonOption = Option.builder("j")
                .longOpt("json")
                .hasArg()
                .argName("path")
                .desc("Path to the JSON file (e.g., data.json)")
                .build();

        Option passOption = Option.builder("p")
                .longOpt("pass")
                .hasArg()
                .argName("path")
                .desc("Path to the password file (e.g., pass.txt)")
                .build();

        options.addOption(jsonOption);
        options.addOption(passOption);
        return options;
    }

    /**
     * 爆破方法
     *
     * @param userList  用户列表，包含 username、password 和 salt 的 Map
     * @param passwords 密码字典
     */
    public static void brute(List<Map<String, String>> userList, List<String> passwords) {
        int totalAttempts = userList.size() * passwords.size();
        // 更新总进度条
        System.out.printf("开始离线爆破，总数: %d\n", totalAttempts);
        // 遍历用户列表
        for (int i = 0; i < userList.size(); i++) {
            Map<String, String> user = userList.get(i); // 获取当前用户
            String username = user.get("username");
            String en_password = user.get("password");
            String salt = user.get("salt");

            // 检查必要字段是否存在
            if (username == null || en_password == null || salt == null) {
                System.err.printf("用户数据格式错误 (用户 %d): 缺少必要的字段 (username, password, salt)\n", i + 1);
                continue;
            }

            // 打印当前用户的进度信息
            System.out.printf("[*] 开始碰撞 %d/%d: %s %s %s\n", i + 1, userList.size(), username, en_password, salt);

            boolean isFound = false; // 是否找到密码的标志

            // 遍历密码字典
            for (int j = 0; j < passwords.size(); j++) {
                String password = passwords.get(j); // 获取当前密码
                String encrypt_password = JeecgBootEncrypt.encrypt(username, password, salt);

                // 如果找到匹配的密码
                if (en_password.equals(encrypt_password)) {
                    System.out.printf("[+] 爆破成功: %s/%s\n", username, password);
                    isFound = true; // 标记为已找到
                    break; // 停止尝试其他密码
                }
            }

            // 如果未找到密码，打印提示信息
            if (!isFound) {
                System.out.printf("[-] 爆破失败 %d/%d: %s\n", i + 1, userList.size(), username);
            }
        }

        System.out.println("\n[*] 所有爆破任务结束");
    }


    public static void main(String[] args) {
        Options options = initOptions();
        // 创建解析器
        CommandLineParser parser = new DefaultParser();

        try {
            // 解析命令行参数
            CommandLine cmd = parser.parse(options, args);
            // 获取参数值（如果没有提供，则使用默认值）
            String jsonFilePath = cmd.hasOption("json") ? cmd.getOptionValue("json") : "data.json";
            String passFilePath = cmd.hasOption("pass") ? cmd.getOptionValue("pass") : "pass.txt";

            // 转换为 Path 对象
            Path jsonPath = Paths.get(jsonFilePath);
            Path passPath = Paths.get(passFilePath);

            if (!Files.exists(jsonPath)) {
                System.out.println(String.format("%s 文件不存在", jsonFilePath));
                return;
            }

            if (!Files.exists(passPath)) {
                System.out.println(String.format("%s 文件不存在", passFilePath));
                return;
            }

            List<Map<String, String>> userList = Utils.loadUserDataFromJson(jsonFilePath);
            List<String> passwords = Files.readAllLines(passPath);
            brute(userList, passwords);

        } catch (IOException e) {
            e.printStackTrace();
        }  catch (ParseException e) {
            // 如果解析失败，打印错误信息和帮助信息
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CommandLineParserExample", options); // 使用 HelpFormatter 打印帮助信息
            System.exit(1);
        }
    }
}


