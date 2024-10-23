package eu.solven.kumite.account;

public record SomeRecord(int i) {
	public static abstract interface SomeInterface {
	}

	public static class SomeClass {
	}

	public class SomeInnerClass {
		{
			System.out.println(i);
		}
	}

	public static abstract class SomeAbstractClass {
	}

	public abstract class SomeAbstractInnerClass {
		{
			System.out.println(i);
		}
	}

	public static abstract @interface SomeAnnotation {
	}
}