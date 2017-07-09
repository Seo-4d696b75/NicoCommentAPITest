import java.util.Scanner;

/**
 * ニコ動のAPIからPOSTで直近のコメントをXMLで取得する実験
 * 日時を表した"version"パラメータをいろいろ変えてみる。
 * Post対象；&lt;thread res_from="-10" version="{?????????}" thread="{スレッドＩＤ}" /&gt;
 * @author Seo-4d696b75
 * @version 2017/07/02
 */
public class CommentAPITest2 extends ExpTemplate {

    protected CommentAPITest2(){}

    private final String postEntity = "<thread res_from=\"-10\" version=\"%s\" thread=\"%d\" />";
    private String path;
    private int threadID;
    private String videoID;

    @Override
    protected void onVideoInfoInput(Scanner scanner){
        System.out.println("put target videoID");
        videoID = scanner.nextLine();
        System.out.println("put threadID of target video");
        threadID = Integer.parseInt(scanner.nextLine());
        path = "http://nmsg.nicovideo.jp/api/";
    }

    @Override
    protected Procedure getProcedure(){
        return new Procedure() {
            @Override
            protected String getResponse(String date) {
                String targetEntity = String.format(postEntity,date,threadID);
                if ( client.post(path,targetEntity) ){
                    return client.getResponse();
                }else{
                    return null;
                }
            }
        };
    }

    @Override
    protected String getDescription(){
        return String.format(
                "target video : %1$s (threadID=%2$d)\ntarget path : %3$s\npost entity : <thread res_from=\"-10\" version=\"{?????????}\" thread=\"%2$d\" />",
                videoID,threadID,path
        );
    }

}
