import java.util.Locale;
import java.util.Scanner;

/**
 * {@link CommentAPITest3}の公式動画版
 * @author Seo-4d696b75
 * @version 2017/07/06
 */
public class CommentAPITest6 extends ExpTemplate {

    private final String postEntity = "<packet><thread thread=\"%2$d\" version=\"%1$s\"  /><thread_leaves thread=\"%2$d\" threadkey=\"%4$s\" force_184=\"%5$d\" >0-%3$d:100,1000</thread_leaves></packet>";
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
                if ( !getThreadKey(threadID) ){
                    return null;
                }
                String targetEntity = String.format(
                        Locale.US,
                        postEntity,
                        date,
                        threadID,
                        length,
                        threadKey,
                        forceValue
                );
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
                "target video : %1$s (threadID=%2$d)\ntarget path : %3$s\npost entity : <packet><thread thread=\"%2$d\" version=\"{??????}\"  /><thread_leaves thread=\"%2$d\" threadkey=\"{??????}\" force_184=\"{?}\">0-%4$d:100,1000</thread_leaves></packet>",
                videoID,threadID,path,length
        );
    }
}
