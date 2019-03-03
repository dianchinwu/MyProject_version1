package tw.tcnr08.a0002;


public interface AsyncResponse {
    //為了取得Async的回傳值,必須實作此介面
    void processFinish(String output);
}
