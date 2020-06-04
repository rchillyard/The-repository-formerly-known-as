package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.util.Arrays;
import java.util.List;

public abstract class QuickSort<X extends Comparable<X>> extends SortWithHelper<X> {

		public QuickSort(String description, int N, Config config) {
				super(description, N, config);
				insertionSort = new InsertionSort<>(getHelper());
		}

		public QuickSort(Partitioner<X> partitioner, Helper<X> helper) {
				super(helper);
				this.partitioner = partitioner;
				insertionSort = new InsertionSort<>(helper);
		}

		/**
		 * Method to create a Partitioner.
		 *
		 * @return a Partitioner of X which is suitable for the quicksort method being used.
		 */
		public abstract Partitioner<X> createPartitioner();

		/**
		 * Method to set the partitioner.
		 * <p>
		 * NOTE: it would be much nicer if we could do this immutably but this isn't Scala, it's Java.
		 *
		 * @param partitioner the partitioner to be used.
		 */
		public void setPartitioner(Partitioner<X> partitioner) {
				this.partitioner = partitioner;
		}

		/**
		 * Method to sort.
		 *
		 * @param xs       sort the array xs, returning the sorted result, leaving xs unchanged.
		 * @param makeCopy if set to true, we make a copy first and sort that.
		 * @return the result (sorted version of xs).
		 */
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
						insertionSort.sort(xs, from, to);
						return;
				}

				Partition<X> partition = createPartition(xs, from, to);
				if (partitioner == null) throw new RuntimeException("partitioner not set");
				List<Partition<X>> partitions = partitioner.partition(partition);
				partitions.forEach(p -> sort(p.xs, p.from, p.to));
		}

		/**
		 * Create a partition on ys from "from" to "to".
		 * @param ys the array to partition
		 * @param from the index of the first element to partition.
		 * @param to the index of the first element NOT to partition.
		 * @param <Y> the underlying type of ys.
		 * @return a Partition of Y.
		 */
		public static <Y extends Comparable<Y>> Partition<Y> createPartition(Y[] ys, int from, int to) {
				return new Partition<>(ys, from, to);
		}

		public static <Y extends Comparable<Y>> Partition<Y> createPartition(Y[] ys) {
				return createPartition(ys, 0, ys.length);
		}

		protected final InsertionSort<X> insertionSort;
		protected Partitioner<X> partitioner;

		final static LazyLogger logger = new LazyLogger(QuickSort.class);
}
