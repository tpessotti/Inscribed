package dev.inscribed.recipe;

import java.util.*;
import java.util.function.BiPredicate;

/** Integer max flow handles split stacks and overlapping tags without greedy false negatives. */
public final class IngredientAllocator {
    public static Optional<int[]> allocate(int[] required, int[] available, BiPredicate<Integer, Integer> matches) {
        int demands = required.length, supplies = available.length, source = demands + supplies, sink = source + 1, n = sink + 1;
        int[][] residual = new int[n][n]; int needed = 0;
        for (int d = 0; d < demands; d++) {
            if (required[d] < 0) throw new IllegalArgumentException("Negative demand");
            needed = Math.addExact(needed, required[d]); residual[source][d] = required[d];
            for (int s = 0; s < supplies; s++) if (matches.test(d, s)) residual[d][demands + s] = required[d];
        }
        for (int s = 0; s < supplies; s++) {
            if (available[s] < 0) throw new IllegalArgumentException("Negative supply");
            residual[demands + s][sink] = available[s];
        }
        int flow = 0;
        while (true) {
            int[] previous = new int[n]; Arrays.fill(previous, -1); previous[source] = source;
            var queue = new ArrayDeque<Integer>(); queue.add(source);
            while (!queue.isEmpty() && previous[sink] < 0) {
                int from = queue.remove();
                for (int to = 0; to < n; to++) if (previous[to] < 0 && residual[from][to] > 0) { previous[to] = from; queue.add(to); }
            }
            if (previous[sink] < 0) break;
            int amount = Integer.MAX_VALUE;
            for (int at = sink; at != source; at = previous[at]) amount = Math.min(amount, residual[previous[at]][at]);
            for (int at = sink; at != source; at = previous[at]) { residual[previous[at]][at] -= amount; residual[at][previous[at]] += amount; }
            flow += amount;
        }
        if (flow != needed) return Optional.empty();
        int[] consumed = new int[supplies];
        for (int s = 0; s < supplies; s++) consumed[s] = available[s] - residual[demands + s][sink];
        return Optional.of(consumed);
    }
    private IngredientAllocator() {}
}
