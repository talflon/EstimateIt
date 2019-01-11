package getzit.net.estimateit;

import java.util.Random;

public interface RandomGenerator<T> {
    T generate(Random random);

    static <T> RandomGenerator<T> flatten(RandomGenerator<? extends RandomGenerator<T>> generator) {
        return random -> generator.generate(random).generate(random);
    }

    static <T> RandomGenerator<T> exactly(T value) {
        return random -> value;
    }

    static <T> RandomGenerator<T> choice(T... options) {
        return random -> options[random.nextInt(options.length)];
    }
}
