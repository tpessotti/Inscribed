package dev.inscribed.recipe;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class IngredientAllocatorTest {
    @Test void overlappingTagsRequireReassignment() {
        var result = IngredientAllocator.allocate(new int[]{1,1}, new int[]{1,1}, (d,s) -> d == 0 || s == 0);
        assertArrayEquals(new int[]{1,1}, result.orElseThrow());
    }
    @Test void splitStacksAndSurplus() {
        assertArrayEquals(new int[]{2,1,0}, IngredientAllocator.allocate(new int[]{3}, new int[]{2,4,5}, (d,s) -> s < 2).orElseThrow());
        assertTrue(IngredientAllocator.allocate(new int[]{7}, new int[]{2,4,5}, (d,s) -> s < 2).isEmpty());
        assertArrayEquals(new int[]{0,0}, IngredientAllocator.allocate(new int[]{}, new int[]{3,4}, (d,s) -> false).orElseThrow());
    }
    @Test void deterministicAgainstExhaustiveSmallInventories() {
        var random = new Random(12345);
        for (int trial = 0; trial < 600; trial++) {
            int nd = 1 + random.nextInt(3), ns = 1 + random.nextInt(4);
            int[] required = new int[nd], supply = new int[ns]; boolean[][] edges = new boolean[nd][ns];
            for (int d = 0; d < nd; d++) { required[d] = random.nextInt(4); for (int s = 0; s < ns; s++) edges[d][s] = random.nextBoolean(); }
            for (int s = 0; s < ns; s++) supply[s] = random.nextInt(4);
            var found = IngredientAllocator.allocate(required, supply, (d,s) -> edges[d][s]);
            assertEquals(possible(required.clone(), supply.clone(), edges, 0), found.isPresent(), "trial " + trial);
            var second = IngredientAllocator.allocate(required, supply, (d,s) -> edges[d][s]);
            if (found.isPresent()) {
                assertArrayEquals(found.get(), second.orElseThrow());
                assertEquals(Arrays.stream(required).sum(), Arrays.stream(found.get()).sum());
                for (int s = 0; s < ns; s++) assertTrue(found.get()[s] <= supply[s]);
            }
        }
    }
    private static boolean possible(int[] required, int[] supply, boolean[][] edges, int d) {
        if (d == required.length) return true;
        if (required[d] == 0) return possible(required, supply, edges, d + 1);
        for (int s = 0; s < supply.length; s++) if (edges[d][s] && supply[s] > 0) {
            supply[s]--; required[d]--;
            boolean found = possible(required, supply, edges, d); supply[s]++; required[d]++;
            if (found) return true;
        }
        return false;
    }
    @Test void invalidCountsFailBeforeMutation() {
        assertThrows(IllegalArgumentException.class, () -> IngredientAllocator.allocate(new int[]{-1}, new int[]{1}, (d,s) -> true));
        assertThrows(IllegalArgumentException.class, () -> IngredientAllocator.allocate(new int[]{1}, new int[]{-1}, (d,s) -> true));
    }
}
