import java.util.Locale;
import java.util.Scanner;

/**
 * {@link CommentAPITest2}の公式動画版
 * @author Seo-4d696b75
 * @version 2017/07/06
 */
public class CommentAPITest5 extends ExpTemplate {

    private final String postEntity = "<thread res_from=\"-10\" version=\"%s\" thread=\"%d\" threadkey=\"%s\" force_184=\"%d\"/>";
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
                "target video : %1$s (threadID=%2$d)\ntarget path : %3$s\npost entity : <thread res_from=\"-10\" version=\"{?????????}\" thread=\"%2$d\" threadkey=\"{?????}\" force_184=\"{?}\"/>",
                videoID,threadID,path
        );
    }


}
