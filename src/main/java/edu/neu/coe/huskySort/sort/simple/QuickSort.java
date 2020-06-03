package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.util.Arrays;
import java.util.List;

public abstract class QuickSort<X extends Comparable<X>> extends SortWithHelper<X> {
		final static LazyLogger logger = new LazyLogger(QuickSort.class);
		protected final InsertionSort<X> insertionSort;
		private int level = 0;

		public void setPartitioner(Partitioner<X> partitioner) {
				this.partitioner = partitioner;
		}

		protected Partitioner<X> partitioner;

		public QuickSort(String description, int N, Config config) {
				super(description, N, false, config);
				insertionSort = new InsertionSort<>(getHelper());
		}

		public QuickSort(Partitioner<X> partitioner, Helper<X> helper) {
				super(helper);
				this.partitioner = partitioner;
				insertionSort = new InsertionSort<>(helper);
		}

		public X[] sort(X[] xs, boolean makeCopy) {
				getHelper().init(xs.length);
				X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
				sort(result, 0, result.length);
				return result;
		}

		/**
		 * Sort the sub-array xs[from] .. xs[to-1]
		 *
		 * @param xs   the complete array from which this sub-array derives.
		 * @param from the index of the first element to sort.
		 * @param to   the index of the first element not to sort.
		 */
		public void sort(X[] xs, int from, int to) {
				@SuppressWarnings("UnnecessaryLocalVariable") int lo = from;
				if (to <= lo + getHelper().cutoff()) {
						logger.debug("insertionsort at level: " + level + " array length: " + xs.length + ", from " + from + " to " + to);
						insertionSort.sort(xs, from, to);
						return;
				}

				logger.debug("quicksort at level: " + level + " array length: " + xs.length + ", from " + from + " to " + to);
				Partition<X> partition = new Partition<>(xs, from, to);
				if (partitioner == null) throw new RuntimeException("partitioner not set");
				List<Partition<X>> partitions = partitioner.partition(partition);
				partitions.forEach(p -> sort(p.xs, p.from, p.to));
		}

}
