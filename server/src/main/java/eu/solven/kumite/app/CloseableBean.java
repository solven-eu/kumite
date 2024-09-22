package eu.solven.kumite.app;

import java.io.Closeable;

import org.springframework.beans.factory.DisposableBean;

import lombok.AllArgsConstructor;

/**
 * Wraps a {@link Closeable} into a {@link DisposableBean}
 * 
 * @author Benoit Lacelle
 *
 */
@AllArgsConstructor
public class CloseableBean implements DisposableBean {
	final AutoCloseable closeable;

	@Override
	public void destroy() throws Exception {
		closeable.close();
	}
}
