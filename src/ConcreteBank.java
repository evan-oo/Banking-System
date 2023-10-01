import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ConcreteBank implements Bank {
// TODO
    Map<String, Integer> bankAccounts = new HashMap<String, Integer>();

    @Override
    public boolean addAccount(String accountID, Integer initBalance) {
        synchronized (bankAccounts){
            if(bankAccounts.get(accountID) == null){
                bankAccounts.put(accountID, initBalance);
                return true;
            }else{
                return false;
            }
        }
    }

    @Override
    public boolean deposit(String accountID, Integer amount) {
        if(bankAccounts.get(accountID) == null){
            return false;
        }else{
            synchronized (bankAccounts){
                int i = bankAccounts.get(accountID) + amount;
                bankAccounts.put(accountID, i);
                bankAccounts.notifyAll();
            }
            return true ;
        }
    }

    @Override
    public boolean withdraw(String accountID, Integer amount, long timeoutMillis) {
        if(bankAccounts.get(accountID) == null){
            return false;
        }
        synchronized (bankAccounts){
            if(bankAccounts.get(accountID) < amount){
                try {
                    bankAccounts.wait(timeoutMillis);
                }catch (InterruptedException e){
                    System.out.println("Withdraw get wrong");
                }
            }
            if(bankAccounts.get(accountID) >= amount){
                int i = bankAccounts.get(accountID) - amount;
                bankAccounts.put(accountID, i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean transfer(String srcAccount, String dstAccount, Integer amount, long timeoutMillis) {
        if(withdraw(srcAccount, amount, timeoutMillis)){
            if(deposit(dstAccount, amount)){
                return true;
            }else{
                deposit(srcAccount, amount);
                return false;
            }
        }
        return false;
    }

    @Override
    public Integer getBalance(String accountID) {
        synchronized (bankAccounts){
            if(bankAccounts.get(accountID) == null){
                return 0;
            }else{
                return bankAccounts.get(accountID);
            }
        }
    }

    @Override
    public void doLottery(ArrayList<String> accounts, Miner miner) {
        /*
        ArrayList<Thread> i = new ArrayList<>();
        for(String ac: accounts){
            Thread t = new Thread(()->{
                //int amount = miner.mine(ac);
                deposit(ac, miner.mine(ac));
            });
            t.start();
            i.add(t);
        }
        for(Thread j: i){
            try {
                j.join();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        */
        ExecutorService executor = Executors.newCachedThreadPool();
        for(String ac: accounts){
            executor.execute(()->deposit(ac, miner.mine(ac)));
        }
        executor.shutdown();
        while(!executor.isTerminated()){

        }
    }
}
