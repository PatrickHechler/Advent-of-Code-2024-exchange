import java.util.Locale;

public class StopWatch22 {

	public interface RunnableWithException {
	    public abstract void run() throws Exception;
	}
	public static void run(RunnableWithException rwe) {
		run(null, rwe);
	}
	public static void run(String watchName, RunnableWithException rwe) {
		StopWatch22 watch = new StopWatch22();
		try {
			rwe.run();
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		finally {
			String seconds = watch.stopStr();
			if (watchName == null) {
				System.out.println("time used: "+seconds+"s");
			}
			else {
				System.out.println("time used for "+watchName+": "+seconds+"s");	
			}
		}
	}
	
	private long startTime;
	
	public StopWatch22() {
		startTime = System.currentTimeMillis();
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
	}
	public double time() {
		return (System.currentTimeMillis()-startTime) * 0.001;
	}
	public double stop() {
		long stopTime = System.currentTimeMillis();
		double result = (stopTime-startTime) * 0.001;
		startTime = stopTime;		
		return result;
	}
	
	public String timeStr() {
		return pretty((System.currentTimeMillis()-startTime) * 0.001);
	}
	public String stopStr() {
		long stopTime = System.currentTimeMillis();
		double result = (stopTime-startTime) * 0.001;
		startTime = stopTime;		
		return pretty(result);
	}
	private static String pretty(double d) {
		return String.format(Locale.ROOT, "%.3f", d);
	}


	private static void test() {
		try {
			StopWatch22.run(() -> Thread.sleep(100));
			try {
				StopWatch22.run("Exceptiontest", () -> {Thread.sleep(100); int x = 5/0;});
				System.err.println("SHOULD NOT BE REACHED");
			}
			catch (Exception ignore) {}
			StopWatch22 watch = new StopWatch22();
			Thread.sleep(100);
			System.out.println("after 100ms: "+watch.time());
			Thread.sleep(100);
			System.out.println("after 200ms: "+watch.time());
			Thread.sleep(100);
			System.out.println("after 300ms: "+watch.stop());
			Thread.sleep(100);
			System.out.println("after 100ms: "+watch.timeStr());
			Thread.sleep(100);
			watch.start();
			Thread.sleep(100);
			System.out.println("after 100ms: "+watch.timeStr());
			Thread.sleep(100);
			System.out.println("after 200ms: "+watch.stopStr());
		}
		catch (InterruptedException ignore) {}
	}
	public static void main(String[] args) { test(); }
	
}
