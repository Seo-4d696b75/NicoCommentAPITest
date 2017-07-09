import java.util.Scanner;

/**
 * ニコ動のAPIからGETで直近のコメントをXMLで取得する実験
 * 日時を表した"version"パラメータをいろいろ変えてみる。<br>
 *  クエリも含んだパス：{messageServerURL}/thread?version={??????}&thread={動画のスレッドＩＤ}&res_from=-10
 *
 * @author Seo-4d696b75
 * @version 2017/07/02
 */
public class CommentAPITest extends ExpTemplate {

    protected CommentAPITest(){}

    private final String path = "%sthread?version=%s&thread=%d&res_from=-10";
    protected String messageServer;
    protected int threadID;
    protected String videoID;

    @Override
    protected void onVideoInfoInput(Scanner scanner){
        System.out.println("put target videoID");
        videoID = scanner.nextLine();
        System.out.println("put threadID of target video");
        threadID = Integer.parseInt(scanner.nextLine());
        messageServer = "http://nmsg.nicovideo.jp/api/";
    }

    @Override
    protected Procedure getProcedure(){
        return new Procedure() {
            @Override
            protected String getResponse(String date) {
                String targetPath = String.format(path,messageServer, date, threadID);
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
                "target video : %1$s (threadID=%2$d)\ntarget path : %3$sthread?version={??????}&thread=%2$d&res_from=-10",
                videoID,threadID,messageServer
        );
    }
}
