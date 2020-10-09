package fx.miserable.sdfs.naming.service;

import fx.miserable.sdfs.naming.exception.EmptyCollectionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RandomService {

	private static final Random random = new Random(17);

	public static Object getAny(Collection<? extends Object> elements) {
		if (elements.size() == 0) {
			throw new EmptyCollectionException("Given Collection is empty.");
		} else {
			return elements.toArray()[random.nextInt(elements.size())];
		}
	}

}
