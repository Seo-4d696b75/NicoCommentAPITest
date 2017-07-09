import java.util.Scanner;

/**
 * ニコ動のAPIからPOSTで適当なコメントをXMLで取得する実験
 * 日時を表した"version"パラメータをいろいろ変えてみる。
 * なお取得できるコメントは直近だけではなく、動画長さに応じた新方式だとか何とか。
 * Post対象；&lt;packet&gt;&lt;thread thread="{スレッドＩＤ}" version="{??????}"  /&gt;&lt;thread_leaves thread="{スレッドＩＤ}"&gt;0-{動画長さ}:100,1000&lt;/thread_leaves&gt;&lt;/packet&gt;<br>
 *     ただし、動画長さは分単位で分未満は切り上げすること。
 * @author Seo-4d696b75
 * @version 2017/07/02
 */
public class CommentAPITest3 extends ExpTemplate{

    protected CommentAPITest3(){}

    private final String postEntity = "<packet><thread thread=\"%2$d\" version=\"%1$s\"  /><thread_leaves thread=\"%2$d\">0-%3$d:100,1000</thread_leaves></packet>";
    private String path;
    private int threadID;
    private String videoID;
    private int length;

    @Override
    protected void onVideoInfoInput(Scanner scanner){
        System.out.println("put target videoID");
        videoID = scanner.nextLine();
        System.out.println("put threadID of target video");
        threadID = Integer.parseInt(scanner.nextLine());
        path = "http://nmsg.nicovideo.jp/api/";
        System.out.println("put length of target video in minutes");
        length = Integer.parseInt(scanner.nextLine());
    }

    @Override
    protected ExpTemplate.Procedure getProcedure(){
        return new ExpTemplate.Procedure() {
            @Override
            protected String getResponse(String date) {
                String targetEntity = String.format(postEntity,date,threadID,length);
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
                "target video : %1$s (threadID=%2$d)\ntarget path : %3$s\npost entity : <packet><thread thread=\"%2$d\" version=\"{??????}\"  /><thread_leaves thread=\"%2$d\">0-%4$d:100,1000</thread_leaves></packet>",
                videoID,threadID,path,length
        );
    }
}
