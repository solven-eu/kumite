package eu.solven.kumite.websocket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import eu.solven.kumite.events.IKumiteContestEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

/**
 * Bridge from the {@link EventBus} into a {@link FluxSink}
 * 
 * @author Benoit Lacelle
 *
 */
@Component
@AllArgsConstructor
@Slf4j
public class ContestsEventPublisher
		implements Consumer<FluxSink<IKumiteContestEvent>>, DisposableBean, ApplicationContextAware {

	private final AtomicReference<ExecutorService> executor = new AtomicReference<>();
	private final BlockingQueue<IKumiteContestEvent> queue = new LinkedBlockingQueue<>();

	@Subscribe
	public void onKumiteContestEvent(IKumiteContestEvent event) {
		// TODO Drop events if too many of them, especially if no websocket connected to the sink
		this.queue.offer(event);
	}

	protected ExecutorService getExecutor() {
		if (executor.get() == null) {
			ExecutorService newEs = Executors.newSingleThreadExecutor();
			if (executor.compareAndSet(null, newEs)) {
				log.info("Registering ES");
			} else {
				log.info("Shutdown ES as another ES has been registered in the meantime");
				newEs.shutdown();
			}
		}

		return executor.get();
	}

	@Override
	public void accept(FluxSink<IKumiteContestEvent> sink) {
		getExecutor().execute(() -> {
			while (true) {
				try {
					IKumiteContestEvent event = queue.take();
					sink.next(event);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					ReflectionUtils.rethrowRuntimeException(e);
				}
			}
		});
	}

	@Override
	public void destroy() throws Exception {
		ExecutorService shutdownMe = getExecutor();
		if (shutdownMe != null) {
			shutdownMe.shutdown();
			log.info("ES is shutdown");
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		applicationContext.getBean(EventBus.class).register(this);
	}
}