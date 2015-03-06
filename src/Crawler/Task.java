package Crawler;

public abstract class Task {
	abstract public boolean InitialCheck(String wid,Client client);
	abstract public boolean run(Worker worker,Client client);
	abstract public boolean login(String wid,Client client);
}
