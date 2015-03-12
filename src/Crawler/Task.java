package Crawler;

public abstract class Task {
	public String username="";
	public String password="";
	abstract public TaskSetting clientRequest();
	abstract public boolean superInit(Worker worker);
	abstract public String InitialCheck(Worker worker,Client client);
	abstract public String run(Worker worker,Client client);
	abstract public String login(Worker worker,Client client);
	abstract public void releaseResources();
	public void taskFail(){};
}
