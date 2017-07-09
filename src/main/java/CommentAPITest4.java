import java.util.Locale;
import java.util.Scanner;

/**
 * {@link CommentAPITest}の公式動画版
 * @author Seo-4d696b75
 * @version 2017/07/06
 */
public class CommentAPITest4 extends ExpTemplate{

    private final String path = "%sthread?version=%s&thread=%d&res_from=-10&threadkey=%s&force_184=%d";
    private String videoID;
    private int threadID;
    private String messageServer;


    @Override
    protected void onVideoInfoInput(Scanner scanner){
        System.out.println("put target videoID");
        videoID = scanner.nextLine();
        System.out.println("put threadID of target video");
        threadID = Integer.parseInt(scanner.nextLine());
        messageServer = "http://nmsg.nicovideo.jp/api/";
    }

    @Override
    protected ExpTemplate.Procedure getProcedure(){
        return new ExpTemplate.Procedure() {
            @Override
            protected String getResponse(String date) {
                if ( !getThreadKey(threadID) ){
                    return null;
                }
                String targetPath = String.format(
                        Locale.US,
                        path,
                        messageServer,
                        date,
                        threadID,
                        threadKey,
                        forceValue
                );
                if (client.get(targetPath)) {
                    return client.getResponse();
                } else {
                    return null;
                }
            }
        };
    }

    @Override
    protected String getDescription(){
        return String.format(
                "target video : %1$s (threadID=%2$d)\ntarget path : %3$sthread?version={??????}&thread=%2$d&res_from=-10&threadkey={?????}&force_184={?}",
                videoID,threadID,messageServer
        );
    }

}
