package de.heuboe.tls.grammar.sequencer.flops;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * This class is a storage that holds and manage all created flops.
 */
@Component
@Slf4j
public class FlopStorage {

    private Map<String, Flop> storage = Collections.synchronizedMap(new HashMap<>());

    /**
     * Returns a {@link Flop} for the passed key from the storage.
     *
     * @param key The key a {@link Flop} should be retrieved for.
     * @return A {@link Flop} for the passed key or null if no {@link Flop} could be found.
     */
    public Flop getFlop(String key) {
        return storage.get(key);
    }

    /**
     * Add or update a {@link Flop} in the storage. The used key will be extracted from the {@link Flop}. If the key is
     * empty or null a warning will be printed and the {@link Flop} will not be added or updated!
     *
     * @param flop The {@link Flop} that should be added to the storage.
     */
    public void addOrUpdateFlop(Flop flop) {
        if (ObjectUtils.isEmpty(flop.getKey())) {
            log.warn("Flop could not be added or updated in storage because the key is of the flop is null or empty!");
        } else {
            if (!exists(flop.getKey())) {
                storage.put(flop.getKey(), flop);
                log.debug("Add flop with key '{}' to storage.", flop.getKey());
            } else {
                storage.remove(flop.getKey());
                storage.put(flop.getKey(), flop);
                log.debug("Update flop with key '{}' in storage.", flop.getKey());
            }
        }
    }

    /**
     * Check if a {@link Flop} exists in the storage that match the passed key.
     *
     * @param key The key the {@link Flop} should be searched for in the storage.
     * @return True if the {@link Flop} for the passed key exists in the storage else false.
     */
    public boolean exists(String key) {
        return storage.containsKey(key);
    }

    /**
     * Check if a {@link Flop} exists in the storage that match the passed {@link Flop}. At this point the {@link Flop}
     * content will not be compared. It will simply extract the key of the {@link Flop} and search for this key in the
     * storage.
     *
     * @param flop The {@link Flop} that should be searched for in the storage.
     * @return True if the {@link Flop} for the passed key exists in the storage else false.
     */
    public boolean exists(Flop flop) {
        return exists(flop.getKey());
    }

    @Scheduled(fixedRate = 60000 )
    private void removeExecutedFlops() {
        Set<String> markedForRemoval = new HashSet<>();

        // check every flop inside the storage ...
        storage.forEach((key, flop) -> {
            // ... if it was marked as destroyable ...
            if (flop.isDestroyable()) {
                // ... and then mark it for removal
                markedForRemoval.add(key);
            }
        });

        // remove marked flops
        markedForRemoval.forEach(flop -> {
            storage.remove(flop);
            log.debug("Removed flop with key '{}' from storage", flop);
        });
    }
}
