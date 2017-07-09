

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * コメント取得の実験を実行するオブジェクトのテンプレート。
 * 指定された日時から日時までに対応したパラメータと共にAPIを叩いてレスポンスを観察する。
 * その際にはマルチスレッドで処理して時間を適宜短縮する。
 * @author Seo-4d696b75
 * @version 2017/07/02
 */
public abstract class ExpTemplate {

    private File out;
    private StringBuilder builder;
    private final String dateFormat = "yyyy-MM-dd";
    private final String urlDateFormat = "yyyyMMdd";
    private Date start,end;
    private Calendar calendar;
    private final int threadNum = 50;
    private final int trialTimes = 3;
    private int finishCnt = 0;

    private final String description ="These are responses from NicoAPI about getting comments.\nFollowing responses are those in case of various \"version\" value. by Seo-4d696b75\n";

    /**
     * 実行します。
     * 実験対象の日時パラメータは基本入力から得られた日時から決定され、
     * 実験結果の出力先のパスも基本入力から指定されます。
     */
    public final void execute(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("set output file path...");
        String path = scanner.nextLine();
        out = new File(path);
        if ( out.exists() ){
            System.out.println("specified file already exists");
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.US);
        try{
            System.out.println("set the start date > " + dateFormat);
            start = format.parse(scanner.nextLine());
            System.out.println("set the end date > " + dateFormat);
            end = format.parse(scanner.nextLine());
            System.out.println("set required information of target video");
            onVideoInfoInput(scanner);
            System.out.println("required information of target video is set");
            System.out.println(
                    String.format(
                            "start date : %s \nend date : %s\noutput file : %s\n start? N/Y",
                            format.format(start),
                            format.format(end),
                            out.getAbsoluteFile()
                    )
            );
            String call = scanner.nextLine();
            if ( call.equals("y") || call.equals("Y") ){
                System.out.println("start....");
                start();
            }else{
                System.out.println("end.....");
            }
        }catch (ParseException e){
            System.out.println("date parse exception....exit");
        }finally {
            scanner.close();
        }
    }

    private void start(){
        builder = new StringBuilder();
        builder.append(description);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.US);
        builder.append("start at ");
        builder.append(format.format(new Date()));
        builder.append("\n");
        format = new SimpleDateFormat(dateFormat,Locale.US);
        builder.append(String.format("start date : %s\n",format.format(start)));
        builder.append(String.format("end date : %s\n",format.format(end)));
        builder.append(getDescription());
        builder.append("\n");
        calendar = Calendar.getInstance();
        calendar.setTime(start);
        status = new int[]{0,0,0,0};
        successList = new ArrayList<>();
        unexpectedList = new ArrayList<>();
        httpFailList = new ArrayList<>();
        for ( int i=0 ; i<threadNum ; i++ ){
            getProcedure().start();
        }
    }

    /**
     * 実験開始前に対象動画の必要な属性を基本入力から取得します。
     * @param scanner
     */
    protected abstract void onVideoInfoInput(Scanner scanner);

    /**
     * 本実験の出力ファイルに記載する適当な説明
     * @return
     */
    protected abstract String getDescription();

    /**
     * 本実験に用いるスレッドを返します。
     * @return {@link Procedure テンプレート}の具体的な処理を定義したサブクラス
     */
    protected abstract Procedure getProcedure();

    /**
     * 本実験の実際のHTTP通信を行うスレッドのテンプレート
     */
    protected abstract class Procedure extends Thread {

        protected Procedure (){
            this.client = new HttpClient();
        }

        private String date;
        protected HttpClient client;

        protected int forceValue;
        protected String threadKey;

        @Override
        public final void run(){
            Date target = getNextDate();
            SimpleDateFormat format = new SimpleDateFormat(urlDateFormat,Locale.US);
            while ( target != null ){
                date = format.format(target);
                trial(date);
                target = getNextDate();
            }
            onThreadFinish();
        }

        private void trial(String date){
            for ( int i=0 ; i<trialTimes ; i++){
                String response = getResponse(date);
                if ( response != null ){
                    switch ( checkStatus(response) ){
                        case STATUS_SUCCESS:
                            onGetResult(String.format("date:%s > success\nresponse;\n%s", date, response),date,STATUS_SUCCESS);
                            break;
                        case STATUS_FAIL:
                            onGetResult(String.format("date:%s > fail",date),date,STATUS_FAIL);
                            break;
                        case STATUS_UNEXPECTED:
                            onGetResult(String.format("date:%s > unexpected response format;\n%s",date,response),date,STATUS_UNEXPECTED);
                            break;
                    }
                    return;
                }
            }
            onGetResult(String.format("date:%s > fail to get response in %d times",date,trialTimes),date,STATUS_HTTP_FAIL);
        }

        private final Pattern threadKeyPattern = Pattern.compile("threadkey=(.+?)&force_184=([0-9])");

        protected boolean getThreadKey(int threadID){
            String path = "http://flapi.nicovideo.jp/api/getthreadkey?thread=" + threadID;
            if ( client.get(path) ){
                Matcher matcher = threadKeyPattern.matcher(client.getResponse());
                if ( matcher.find() ){
                    threadKey = matcher.group(1);
                    forceValue = Integer.parseInt(matcher.group(2));
                    return true;
                }
            }
            return false;
        }

        /**
         * HTTP通信してレスポンスを取得します
         * @param date 実験対象の日時パラメータ
         * @return 通信に失敗したら{@code null}
         */
        protected abstract String getResponse(String date);

    }

    private final int STATUS_SUCCESS  = 0;
    private final int STATUS_FAIL = 1;
    private final int STATUS_HTTP_FAIL = 2;
    private final int STATUS_UNEXPECTED = 3;
    private int[] status;
    private List<String> successList;
    private List<String> unexpectedList;
    private List<String> httpFailList;

    private int checkStatus(String response){
        Matcher matcher = statusPattern.matcher(response);
        //statusCodeを複数含むレスポンスが発見されたため変更
        if ( matcher.find() ){
            do{
                if ( matcher.group(1).equals("0") ){
                    return STATUS_SUCCESS;
                }
            }while( matcher.find() );
            return STATUS_FAIL;
        }
        return STATUS_UNEXPECTED;
    }

    private final Pattern statusPattern = Pattern.compile("<thread resultcode=\"([0-9])\" thread=");

    private synchronized void onThreadFinish(){
        finishCnt++;
        if ( finishCnt == threadNum ){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.US);
            builder.append("finished at ");
            builder.append(format.format(new Date()));
            builder.append("\n");
            String totalResult = getTotalResult();
            System.out.println(totalResult);
            builder.append(totalResult);
            System.out.println("...........finnish");
            if ( writeFile(out,builder.toString()) ){
                System.out.println("succeed to write file....exit");
            }else{
                System.out.println("fail to write file...exit");
            }
        }
    }

    private String getTotalResult(){
        StringBuilder builder = new StringBuilder();
        builder.append("result;\n");
        builder.append(String.format("total : %d\n",status[STATUS_SUCCESS]+status[STATUS_FAIL]+status[STATUS_UNEXPECTED]+status[STATUS_HTTP_FAIL]));
        builder.append(String.format("fail : %d\n",status[STATUS_FAIL]));
        builder.append(String.format("success : %d > %s\n",status[STATUS_SUCCESS],successList.toString()));
        builder.append(String.format("unexpected response : %d > %s\n",status[STATUS_UNEXPECTED],unexpectedList.toString()));
        builder.append(String.format("http fail : %d > %s",status[STATUS_HTTP_FAIL],httpFailList.toString()));
        return builder.toString();
    }

    private synchronized void onGetResult(String result,String date, int status){
        builder.append(result);
        builder.append("\n");
        System.out.println(result);
        this.status[status]++;
        switch (status){
            case STATUS_SUCCESS:
                successList.add(date);
                break;
            case STATUS_UNEXPECTED:
                unexpectedList.add(date);
                break;
            case STATUS_HTTP_FAIL:
                httpFailList.add(date);
                break;
            default:
        }
    }

    private synchronized Date getNextDate(){
        Date next = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH,1);
        return next.compareTo(end) > 0 ? null : next;
    }

    private boolean writeFile(File target, String content){
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target),"UTF8"));
            writer.write(content);
            writer.close();
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

}
