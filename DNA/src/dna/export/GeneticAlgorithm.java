package dna.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

public class GeneticAlgorithm {
	
	ArrayList<PolarizationResult> polarizationResults;
	
	/**
	 * Genetic optimization of polarization. This algorithm finds the extent of
	 * polarization of a given discourse network by optimizing for the partition
	 * into k equally-sized clusters that maximizes a combined quality measure
	 * taking into account congruence and conflict. The user can choose network
	 * modularity or the E-I index for the quality measure. Optimization is done
	 * by applying a genetic algorithm to the quality of the respective cluster
	 * solutions with k levels.
	 * 
	 * @param k                    Number of clusters, usually 2.
	 * @param numClusterSolutions  Population size; number of cluster solutions in each generation. Suggested values are around 30-50.
	 * @param iterations           Maximal number of generations through which optimization should be attempted. Will be lower if early convergence is detected. A suggested starting value is 1000. 
	 * @param qualityFunction      The function used to assess the quality, or fitness, of a cluster solution. Can be "modularity" (for modularity) or "ei" (for Krackhardt's E-I index).
	 * @param eliteShare           The fraction of highest-quality cluster solutions in the parent generation that should be retained as is in the children generation. A suggested value is 0.2. 
	 * @param mutationShare        The fraction of nodes in each cluster solution for which cluster memberships should be randomly mutated after cross-over. For example, a value of 0.2 will select 10% of the nodes plus another 10% as targets to swap their cluster membership with.
	 * @throws Exception
	 */
	public GeneticAlgorithm(
			ExporterR exporterR,
			String statementType,
			String variable1,
			boolean variable1Document,
			String variable2,
			boolean variable2Document,
			String qualifier,
			String normalization,
			String duplicates,
			String startDate,
			String stopDate,
			String startTime,
			String stopTime,
			String timewindow,
			int windowsize,
			String[] excludeVariables,
			String[] excludeValues,
			String[] excludeAuthors,
			String[] excludeSources,
			String[] excludeSections,
			String[] excludeTypes,
			boolean invertValues,
			boolean invertAuthors,
			boolean invertSources,
			boolean invertSections,
			boolean invertTypes,
			int k,
			int numClusterSolutions,
			int iterations,
			String qualityFunction,
			double eliteShare,
			double mutationShare
			) throws Exception {
		
		exporterR.rNetwork( // call rNetwork to compute the congruence network(s) (and later again for the conflict network(s))
				"onemode", // networkType
				statementType,
				variable1,
				variable1Document,
				variable2,
				variable2Document,
				qualifier,
				"congruence", // qualifierAggregation
				normalization,
				false, // includeIsolates
				duplicates,
				startDate,
				stopDate,
				startTime,
				stopTime,
				timewindow,
				windowsize,
				excludeVariables,
				excludeValues,
				excludeAuthors,
				excludeSources,
				excludeSections, 
				excludeTypes,
				invertValues,
				invertAuthors,
				invertSources,
				invertSections,
				invertTypes,
				null, // outfile
				null, // fileFormat
				false // verbose
				);
		
		int i;
		ArrayList<Matrix> congruenceList = new ArrayList<Matrix>();
		for (i = 0; i < exporterR.matrices.size(); i++) {
			congruenceList.add((Matrix) exporterR.matrices.get(i).clone());
		}
		
		exporterR.rNetwork(
				"onemode", // networkType
				statementType,
				variable1,
				variable1Document,
				variable2,
				variable2Document,
				qualifier,
				"conflict", // qualifierAggregation
				normalization,
				false, // includeIsolates
				duplicates,
				startDate,
				stopDate,
				startTime,
				stopTime,
				timewindow,
				windowsize,
				excludeVariables,
				excludeValues,
				excludeAuthors,
				excludeSources,
				excludeSections, 
				excludeTypes,
				invertValues,
				invertAuthors,
				invertSources,
				invertSections,
				invertTypes,
				null, // outfile
				null, // fileFormat
				false // verbose
				);

		ArrayList<Matrix> conflictList = new ArrayList<Matrix>();
		for (i = 0; i < exporterR.matrices.size(); i++) {
			conflictList.add((Matrix) exporterR.matrices.get(i).clone());
		}

		run(numClusterSolutions,
				k,
				iterations,
				eliteShare,
				mutationShare,
				qualityFunction,
				congruenceList,
				conflictList);
	}
	
	public ArrayList<PolarizationResult> getResults() {
		return this.polarizationResults;
	}

	/**
	 * Prepare the genetic algorithm and run all the iterations over all time steps.
	 * Take out the maximum quality measure at the last step and create an object
	 * that stores the polarization results.
	 * 
	 * @param numClusterSolutions  How many cluster solutions should be in each generation?
	 * @param k                    How many clusters of equal size should there be?
	 * @param iterations           For how many generations should the genetic algorithm run?
	 * @param eliteShare           The share of cluster solutions in each parent generation that is copied into the children generation without changes, between 0.0 and 1.0, usually around 0.1 when there are many or 0.2 when there are few cluster solutions per generation.
	 * @param mutationShare        The probability with which each bit in any cluster solution is selected for mutation after the cross-over step. For example 0.2 to select 20% of the nodes to swap their memberships.
	 * @param qualityFunction      Which quality or fitness function should be used? Valid values are "modularity" and "ei".
	 * @param congruenceList       Array list of congruence network matrices.
	 * @param conflictList         Array list of conflict network matrices.
	 * @throws Exception
	 */
	private void run (
			int numClusterSolutions,
			int k,
			int iterations,
			double eliteShare,
			double mutationShare,
			String qualityFunction,
			ArrayList<Matrix> congruenceList,
			ArrayList<Matrix> conflictList
			) throws Exception {
		
		boolean verbose = false;
		
		// create variables and check validity of arguments
		polarizationResults = new ArrayList<PolarizationResult>();
		int i, numNodes;
		if (k < 2) {
			throw new Exception("There must be at least k = 2 clusters.");
		}
		if (numClusterSolutions % 2 != 0 || numClusterSolutions < 3) {
			throw new Exception("numClusterSolutions must be an even number above 2.");
		}
		if (congruenceList.size() != conflictList.size()) {
			throw new Exception("Congruence and conflict lists have different sizes.");
		}
		
		// for each time step, run the genetic algorithm over the cluster solutions
		// for a certain number of iterations; retain max/mean/SD quality and memberships
		double[][] congruence, conflict;
		double[] qualityScores;
		double maxQ;
		double avgQ, sdQ;
		int j, t;
		int maxIndex = -1;
		double[] maxQArray;
		double[] avgQArray;
		double[] sdQArray;
		boolean earlyConvergence = false;
		int lastIndex = -1;
		for (t = 0; t < congruenceList.size(); t++) { // go through all time steps of the time window networks
			maxQArray = new double[iterations];
			avgQArray = new double[iterations];
			sdQArray = new double[iterations];
			if (congruenceList.size() > 1) {
				System.out.println("Time step: " + t);
			}
			congruence = congruenceList.get(t).getMatrix();
			conflict = conflictList.get(t).getMatrix();
			
			if (congruenceList.get(t).getMatrix().length > 0) { // if the network has no nodes, skip this step and return 0 directly

				// create an array list of cluster solutions with random memberships
				numNodes = congruence.length;
				ArrayList<ClusterSolution> cs = new ArrayList<ClusterSolution>();
				for (i = 0; i < numClusterSolutions; i++) {
					cs.add(new ClusterSolution(numNodes, k));
				}
				
				// run through iterations and do the breeding, then collect results and stats
				lastIndex = iterations - 1; // choose last possible value here as a default if early convergence does not happen
				for (i = 0; i < iterations; i++) {
					cs = iteration(
							cs,
							congruence,
							conflict,
							qualityFunction,
							numNodes,
							eliteShare,
							mutationShare,
							k); // iteration step

					// compute summary statistics based on iteration step and retain them
					qualityScores = new double[numClusterSolutions];
					maxQ = -1.0;
					avgQ = 0.0;
					sdQ = 0.0;
					maxIndex = -1;
					for (j = 0; j < cs.size(); j++) {
						// cs.get(j).validateMemberships(qualityFunction); // check if the membership vector still has a uniform size distribution and/or valid k levels
						if (qualityFunction.equals("modularity")) {
							qualityScores[j] = cs.get(j).qualityModularity(congruence, conflict);
						} else if (qualityFunction.equals("ei")) {
							qualityScores[j] = cs.get(j).qualityEI(congruence, conflict);
						} else {
							throw new Exception("Quality function '" + qualityFunction + "' not supported.");
						}
						avgQ += qualityScores[j];
						if (qualityScores[j] > maxQ) {
							maxQ = qualityScores[j];
							maxIndex = j;
						}
					}
					avgQ = avgQ / numClusterSolutions;
					for (j = 0; j < numClusterSolutions; j++) {
						sdQ = sdQ + Math.sqrt(((qualityScores[j] - avgQ) * (qualityScores[j] - avgQ)) / numClusterSolutions);
					}
					if (verbose == true) {
						System.out.printf("Max Q: %.2f. Mean Q: %.2f. SD Q: %.2f.\n", maxQ, avgQ, sdQ);
					}
					maxQArray[i] = maxQ;
					avgQArray[i] = avgQ;
					sdQArray[i] = sdQ;
					
					// check early convergence
					earlyConvergence = true;
					if (i >= 10 && (double) Math.round(sdQ * 100) / 100 == 0.00 && (double) Math.round(maxQ * 100) / 100 == (double) Math.round(avgQ * 100) / 100) {
						for (j = i - 10; j < i; j++) {
							if ((double) Math.round(maxQArray[j] * 100) / 100 != (double) Math.round(maxQ * 100) / 100 ||
									(double) Math.round(avgQArray[j] * 100) / 100 != (double) Math.round(avgQ * 100) / 100 ||
											(double) Math.round(sdQArray[j] * 100) / 100 != 0.00) {
								earlyConvergence = false;
							}
						}
					} else {
						earlyConvergence = false;
					}
					if (earlyConvergence == true) {
						if (verbose == true) {
							System.out.println("Early convergence detected after iteration " + i + ". Stopping here and saving result.");
						}
						lastIndex = i;
						break;
					}
				}
				
				// correct for early convergence in results vectors
				if (lastIndex < iterations - 1) {
					double[] maxQArrayTemp = new double[lastIndex + 1];
					double[] avgQArrayTemp = new double[lastIndex + 1];
					double[] sdQArrayTemp = new double[lastIndex + 1];
					for (i = 0; i < lastIndex + 1; i++) {
						maxQArrayTemp[i] = maxQArray[i];
						avgQArrayTemp[i] = avgQArray[i];
						sdQArrayTemp[i] = sdQArray[i];
					}
					maxQArray = maxQArrayTemp;
					avgQArray = avgQArrayTemp;
					sdQArray = sdQArrayTemp;
				}
				
				// save results in array as a complex object
				PolarizationResult pr = new PolarizationResult(
						maxQArray.clone(),
						avgQArray.clone(),
						sdQArray.clone(),
						maxQArray[lastIndex],
						cs.get(maxIndex).getMemberships().clone(),
						congruenceList.get(t).getRownames(),
						earlyConvergence,
						congruenceList.get(t).getStart(),
						congruenceList.get(t).getStop(),
						congruenceList.get(t).getDate());
				polarizationResults.add(pr);
			} else { // zero result because network is empty
				PolarizationResult pr = new PolarizationResult(
						new double[] { 0 },
						new double[] { 0 },
						new double[] { 0 },
						0.0,
						new int[0],
						new String[0],
						true,
						congruenceList.get(t).getStart(),
						congruenceList.get(t).getStop(),
						congruenceList.get(t).getDate());
				polarizationResults.add(pr);
			}
		}
	}
	
	/**
	 * Execute a single iteration of the genetic algorithm, including elite retention, cross-over, and mutation.
	 * 
	 * @param clusterSolutions  The parent generation of cluster solutions as an array list.
	 * @param congruence        A congruence network as a two-dimensional double array.
	 * @param conflict          A conflict network as a two-dimensional double array.
	 * @param qualityFunction   Quality of fitness function to use ("modularity" or "ei").
	 * @param n                 The number of nodes in the network, which is also the number of bits in each membership vector.
	 * @param eliteShare        The share of cluster solutions to copy into the children generation without modification.
	 * @param mutationShare     The probability with which each membership bit is selected for mutation (by swapping membership with another node).
	 * @param k                 The number of clusters of equal size.
	 * @return                  An array list with the children generation of cluster solutions.
	 * @throws Exception
	 */
	private ArrayList<ClusterSolution> iteration (
			ArrayList<ClusterSolution> clusterSolutions,
			double[][] congruence,
			double[][] conflict,
			String qualityFunction,
			int n,
			double eliteShare,
			double mutationShare,
			int k) throws Exception {
		
		int numClusterSolutions = clusterSolutions.size();
		int elites = (int) Math.ceil(eliteShare * numClusterSolutions);
		int mutantChromosomes = (int) Math.round((mutationShare / 2) * n); // for how many pairs should we swap cluster memberships (i.e., half the number of nodes)? 
		int i, i2, j;
		
		// compute quality for all initial solutions; if not even the elite share is positive, sample completely new numbers
		double[] q = new double[numClusterSolutions]; // quality values for all cluster solutions
		int positiveQ = 0;
		
		while (positiveQ < elites) {
			positiveQ = 0;
			for (i = 0; i < clusterSolutions.size(); i++) {
				if (qualityFunction.equals("modularity")) {
					q[i] = clusterSolutions.get(i).qualityModularity(congruence, conflict);
				} else if (qualityFunction.equals("ei")) {
					q[i] = clusterSolutions.get(i).qualityEI(congruence, conflict);
				} else {
					throw new Exception("Quality function '" + qualityFunction + "' not supported.");
				}
			}
			for (i = 0; i < numClusterSolutions; i++) {
				if (q[i] > 0) {
					positiveQ++;
				}
			}
			if (positiveQ < elites) { // resample initial cluster solution until there are at least some positive modularity values in the initial solution
				clusterSolutions.clear();
				for (i = 0; i < numClusterSolutions; i++) {
					clusterSolutions.add(new ClusterSolution(n, k));
				}
			}
		}

		// compute ranks of quality values
		int[] qRanks = calculateRanks(q);

		// select elite children by considering most highly ranked quality values
		ArrayList<ClusterSolution> children = new ArrayList<ClusterSolution>();
		for (i = 0; i < qRanks.length; i++) {
			if (qRanks[i] < elites) {
				children.add((ClusterSolution) clusterSolutions.get(i).clone());
			}
		}
		
		// replace all negative quality values by zero; otherwise the roulette sampling wouldn't work
		for (i = 0; i < q.length; i++) {
			if (q[i] < 0) {
				q[i] = 0;
			}
		}
		
		// compute total non-negative quality over all cluster solutions for roulette sampling
		double qTotal = 0.0;
		for (i = 0; i < q.length; i++) {
			qTotal += q[i];
		}

		// weighted (roulette) sampling of two individuals according to their
		// quality scores, then cross-over step and add to list of children
		Random rand = new Random();
		double cumulative1, cumulative2;
		ClusterSolution c;
		while (children.size() < numClusterSolutions) {
			double r = rand.nextDouble() * qTotal;
			cumulative1 = 0.0;
			for (i = 0; i < q.length; i++) {
				cumulative1 += q[i];
				if (r <= cumulative1) {
					double r2 = rand.nextDouble() * qTotal;
					cumulative2 = 0.0;
					for (i2 = 0; i2 < q.length; i2++) {
						cumulative2 += q[i2];
						if (r2 <= cumulative2) {
							c = (ClusterSolution) clusterSolutions.get(i).clone();
							c.crossOver(clusterSolutions.get(i2).getMemberships());
							children.add(c);
							break;
						}
					}
					break;
				}
			}
		}

		// mutation step: select some percentage of the non-elite chromosomes (governed by the
		// mutantChromosomes parameter) as pairs and swap around their cluster membership;
		// if the solutions are not constrained to equal sizes (penalty method), do not swap
		// memberships, but simply toggle them with a certain probability instead
		int[] mem;
		ArrayList<MembershipPair> mutationPairs = new ArrayList<MembershipPair>();
		boolean contained;
		for (i = elites; i < numClusterSolutions; i++) {
			mem = children.get(i).getMemberships();
			mutationPairs.clear();
			int firstIndex = -1, secondIndex = -1, firstK = 0, secondK = 1;
			contained = true;
			while (mutationPairs.size() < mutantChromosomes) {
				firstIndex = rand.nextInt(n);
				secondIndex = rand.nextInt(n);
				firstK = mem[firstIndex];
				secondK = mem[secondIndex];
				contained = false;
				for (j = 0; j < mutationPairs.size(); j++) { // check if a pair was randomly chosen in which at least one node had already been sampled
					if (mutationPairs.get(j).getFirstIndex() == firstIndex ||
							mutationPairs.get(j).getSecondIndex() == secondIndex ||
							mutationPairs.get(j).getFirstIndex() == secondIndex ||
							mutationPairs.get(j).getSecondIndex() == firstIndex) {
						contained = true;
					}
				}
				if (firstIndex != secondIndex && firstK == secondK && contained == false) {
					mutationPairs.add(new MembershipPair(firstIndex, secondIndex));
				}
			}
			for (j = 0; j < mutationPairs.size(); j++) { // swap each pair's cluster memberships
				firstK = mem[firstIndex];
				secondK = mem[secondIndex];
				mem[firstIndex] = secondK;
				mem[secondIndex] = firstK;
			}
		}

		return children;
	}

	/**
	 * This class represents a cluster solution, including the membership
	 * vector, which contains information on cluster membership for each node in
	 * the network. It also contains the number of nodes N and the number of
	 * clusters K. In terms of methods, the class can populate its membership
	 * vector with random values with the given number of clusters and nodes; it
	 * can do the cross-over with a foreign membership vector and return
	 * offspring; and it can determine its quality with regard to a congruence
	 * and conflict network using modularity or the E-I index.
	 */
	private class ClusterSolution implements Cloneable {
		
		int[] memberships; // cluster memberships of all nodes as integer values, starting with 0
		int N; // number of nodes
		int K; // number of clusters
		
		public ClusterSolution(int n, int k) {
			this.N = n;
			this.K = k;
			this.createRandomMemberships();
		}
		
		// it is necessary to implement Cloneable to avoid passing the object from the parent
		// generation to the children generation as a reference only; this would lead to indirect
		// changes of the elite children via the parent generation otherwise
		protected Object clone() throws CloneNotSupportedException {
	        return super.clone();
	    }
		
		public int[] getMemberships() {
			return memberships;
		}
		
		private void createRandomMemberships() {
			int i;
			ArrayList<Integer> membership = new ArrayList<Integer>();
			while (membership.size() < N) {
				for (i = 0; i < K; i++) {
					membership.add(i);
				}
			}
			i = K - 1;
			while (membership.size() > N) {
				membership.remove(i);
				i--;
			}
			Collections.shuffle(membership);
			int[] membershipArray = new int[N];
			for (i = 0; i < N; i++) {
				membershipArray[i] = membership.get(i);
			}
			this.memberships = membershipArray;
		}
		
		/**
		 * Cross-over breeding. This function takes a foreign and the domestic membership vectors, relabels the
		 * cluster levels to make them comparable, and creates an offspring version in which the different bits
		 * are randomly combined between the domestic and the foreign cluster solution.
		 * 
		 * @param foreignMemberships  A vector of memberships of a foreign cluster solution.
		 * @throws Exception
		 */
		public void crossOver(int[] foreignMemberships) throws Exception {
			if (foreignMemberships.length != this.memberships.length) {
				throw new Exception("Cross-over attempt failed due to incompatible membership vector lengths.");
			}
			
			int i, j;
			
			// create a K x K matrix indicating which k of the own membership
			// vector maps onto which k of another cluster solution how often
			int[][] kk = new int[K][K];
			for (i = 0; i < N; i++) {
				kk[this.memberships[i]][foreignMemberships[i]]++;
			}
			
			// find the maximum values for all rows and columns, respectively; these
			// are used to relabel the cluster levels to put them on a joint scale;
			// the level with the highest overlap with the other organism becomes the 
			// new number one, the level with the second highest overlap becomes the 
			// new number two etc., from both the domestic (row) perspective and the 
			// foreign (column) perspective; by doing this, the k level with the highest 
			// joint membership count becomes the same new shared k level etc.
			int[] rowMax = new int[K];
			for (i = 0; i < K; i++) {
				for (j = 0; j < K; j++) {
					if (kk[i][j] > rowMax[i]) {
						rowMax[i] = kk[i][j];
					}
				}
			}
			int[] colMax = new int[K];
			for (i = 0; i < K; i++) {
				for (j = 0; j < K; j++) {
					if (kk[i][j] > colMax[j]) {
						colMax[j] = kk[i][j];
					}
				}
			}
			
			// go through all cluster levels; determine the maximum overlap value;
			// determine which row or column index holds this value; save the index
			// as the new next best cluster level and blacklist it for the next iteration
			int[] newClusterLevelsRow = calculateRanks(rowMax);
			int[] newClusterLevelsCol = calculateRanks(colMax);
			
			// for all membership bits, replace the level with the new level, from both 
			// organisms' perspectives; this establishes comparable cluster membership chromosomes
			int[] newRowMem = new int[N];
			int[] newColMem = new int[N];
			for (i = 0; i < N; i++) {
				newRowMem[i] = newClusterLevelsRow[this.memberships[i]];
				newColMem[i] = newClusterLevelsCol[foreignMemberships[i]];
			}
			
			// cross-over: swap with a probability of 0.5
			Random rand = new Random();
			for (i = 0; i < newRowMem.length; i++) {
				if (rand.nextInt(2) == 1) {
					newRowMem[i] = newColMem[i];
				}
			}
			
			// determine distribution of k levels
			double expectation = Math.ceil(N / K);
			int[] sums = new int[K];
			for (i = 0; i < newRowMem.length; i++) {
				sums[newRowMem[i]]++;
			}

			// put indices in a nested array list
			ArrayList<ArrayList<Integer>> indicesArray = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> indices;
			for (i = 0; i < sums.length; i++) {
				indices = new ArrayList<Integer>();
				for (j = 0; j < N; j++) {
					if (newRowMem[j] == i) {
						indices.add(j);
					}
				}
				indicesArray.add(indices);
			}
			
			// fixing K distribution: compare the distribution of the K levels to a uniform distribution;
			// recode a random over-represented chromosome into an underrepresented chromosome;
			// keep repeating until the distribution is equal
			int index;
			for (i = 0; i < sums.length; i++) {
				for (j = 0; j < sums.length; j++) {
					while (sums[i] > expectation && sums[j] < expectation) { //  && sums[i] - sums[j] > 2
						indices = indicesArray.get(i);
						index = rand.nextInt(indices.size());
						int indexedValue = indices.get(index);
						newRowMem[indexedValue] = j;
						
						Iterator<Integer> itr = indices.iterator();
				        while (itr.hasNext()) { 
				            int x = (Integer) itr.next(); 
				            if (x == indexedValue) {
				                itr.remove();
				            } 
				        }
				        indicesArray.set(i, indices);
						indicesArray.get(j).add(indexedValue);
						Collections.sort(indicesArray.get(j));
						sums[i]--;
						sums[j]++;
					}
				}
			}
			
			// determine distribution of k levels
			sums = new int[K];
			for (i = 0; i < newRowMem.length; i++) {
				sums[newRowMem[i]]++;
			}
			
			this.memberships = newRowMem;
		}
		
		/**
		 * Compute the E-I index by Krackhardt.
		 * 
		 * @param mat  The network matrix for which the index should be computed.
		 * @return     The E-I score.
		 */
		private double ei(double[][] mat ) {
			double external = 0.0;
			double internal = 0.0;
			int i, j;

			// remove negative values from matrix
			for (i = 0; i < mat.length; i++) {
				for (j = 0; j < mat[0].length; j++) {
					if (mat[i][j] < 0.0) {
						mat[i][j] = 0.0;
					}
				}
			}
			
			for (i = 0; i < mat.length; i++) {
				for (j = 0; j < mat[0].length; j++) {
					if (i < j) {
						if (memberships[i] == memberships[j]) {
							internal += mat[i][j];
						} else if (memberships[i] != memberships[j]) {
							external += mat[i][j];
						}
					}
				}
			}
			double ei = (external - internal) / (external + internal);
			return ei;
		}
		
		/**
		 * A quality or fitness function that combines the E-I index of a congruence and a conflict network.
		 * 
		 * @param congruence  A network matrix with positive/agreement/congruence ties.
		 * @param conflict    A network matrix with negative/disagreement/conflict ties.
		 * @return            A score that indicates how polarized a given pair of congruence and conflict networks is.
		 */
		private double qualityEI(double[][] congruence, double[][] conflict) {
			double eiCongruence = ei(congruence);
			double eiConflict = ei(conflict);
			return (eiConflict / 2) - (eiCongruence / 2); // subtract congruence from conflict score because high EI means many between-coalition ties
		}
		
		/**
		 * Compute Newman's modularity score for a given binary or weighted network matrix.
		 * 
		 * @param mat  The network matrix for which the index should be computed.
		 * @return     The modularity score.
		 */
		private double modularity(double[][] mat) {
			int i, j, k = 0;
			
			// enumerate unique cluster memberships
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (i = 0; i < K; i++) {
				values.add(i);
			}

			// create 'e' matrix for fraction of ties within/across communities
			double[][] e = new double[K][K];
			double total = 0.0;
			for (i = 0; i < this.memberships.length; i++) {
				for (j = 0; j < this.memberships.length; j++) {
					e[this.memberships[i]][this.memberships[j]] += mat[i][j];
					total += mat[i][j];
				}
			}
			for (i = 0; i < e.length; i++) {
				for (j = 0; j < e[0].length; j++) {
					e[i][j] = e[i][j] / total;
				}
			}

			// first part of Newman (2004) equation 4: the Trace of 'e'
			double tr = 0.0;
			for (i = 0; i < e.length; i++) {
				tr += e[i][i];
			}
			
			// second part of Newman (2004) equation 4: the random expectation
			double b = 0.0;
			for (i = 0; i < values.size(); i++) {
				for (j = 0; j < values.size(); j++) {
					for (k = 0; k < values.size(); k++) {
						b += (e[i][j] * e[k][i]);
					}
				}
			}
			
			return tr - b;
		}
		
		/**
		 * A quality or fitness function that combines the modularity of a congruence and a conflict network.
		 * 
		 * @param congruence  A network matrix with positive/agreement/congruence ties.
		 * @param conflict    A network matrix with negative/disagreement/conflict ties.
		 * @return            A score that indicates how polarized a given pair of congruence and conflict networks is.
		 */
		private double qualityModularity(double[][] congruence, double[][] conflict) throws Exception {
			double modCongruence = modularity(congruence);
			double modConflict = modularity(conflict);
			return (modCongruence / 2) - (modConflict / 2);  // subtract conflict from congruence modularity; if lots of conflict, a negative value will be subtracted, i.e., something is added
		}
	}

	/**
	 * For a given double array, rank its values, starting at 0.
	 * 
	 * @param arr  A double array.
	 * @return     A vector of ranks, starting with 0.
	 */
	public int[] calculateRanks(double... arr) {
	    class Pair {
	        final double value;
	        final int index;

	        Pair(double value, int index) {
	            this.value = value;
	            this.index = index;
	        }
	    }

	    Pair[] pairs = new Pair[arr.length];
	    for (int index = 0; index < arr.length; ++index) {
	        pairs[index] = new Pair(arr[index], index);
	    }

	    Arrays.sort(pairs, (o1, o2) -> -Double.compare(o1.value, o2.value));

	    int[] ranks = new int[arr.length];
	    ranks[pairs[0].index] = 1;
	    int i;
	    for (i = 1; i < pairs.length; ++i) {
	    	ranks[pairs[i].index] = i + 1;
	    }
	    for (i = 0; i < ranks.length; ++i) {
	    	ranks[i]--;
	    }
	    return ranks;
	}

	/**
	 * For a given int array, rank its values, starting at 0.
	 * 
	 * @param arr  An int array.
	 * @return     A vector of ranks, starting with 0.
	 */
	public int[] calculateRanks(int... arr) {
	    class Pair {
	        final int value;
	        final int index;

	        Pair(int value, int index) {
	            this.value = value;
	            this.index = index;
	        }
	    }

	    Pair[] pairs = new Pair[arr.length];
	    for (int index = 0; index < arr.length; ++index) {
	        pairs[index] = new Pair(arr[index], index);
	    }

	    Arrays.sort(pairs, (o1, o2) -> -Integer.compare(o1.value, o2.value));

	    int[] ranks = new int[arr.length];
	    ranks[pairs[0].index] = 1;
	    int i;
	    for (i = 1; i < pairs.length; ++i) {
	    	ranks[pairs[i].index] = i + 1;
	    }
	    for (i = 0; i < ranks.length; ++i) {
	    	ranks[i]--;
	    }
	    return ranks;
	}


	/**
	 * Define a class that represents pairs of two indices of membership
	 * bits (i.e., index of the first node and index of the second node in a
	 * membership solution, with a maximum of N nodes.
	 */
	class MembershipPair {
		int firstIndex;
		int secondIndex;
		
		public MembershipPair(int firstIndex, int secondIndex) {
			this.firstIndex = firstIndex;
			this.secondIndex = secondIndex;
		}
		
		public int getFirstIndex() {
			return this.firstIndex;
		}

		public int getSecondIndex() {
			return this.secondIndex;
		}
	}
	
	
	/**
	 * Store the results of a single run of the genetic algorithm, i.e., for a
	 * single time step of the time window algorithm or the whole network if no
	 * time window was set.
	 */
	public class PolarizationResult {
		double[] maxQ;
		double[] avgQ;
		double[] sdQ;
		double finalMaxQ;
		int[] memberships;
		String[] names;
		boolean earlyConvergence;
		Date start, stop, middle;

		/**
		 * Create a polarization result for a single time step.
		 * 
		 * @param maxQ              The maximum quality score for each iteration of the genetic algorithm.
		 * @param avgQ              The mean quality score for each iteration of the genetic algorithm.
		 * @param sdQ               The standard deviation of the quality scores for each iteration of the genetic algorithm.
		 * @param finalMaxQ         The maximum quality score of the final iteration of the genetic algorithm.
		 * @param memberships       A membership array containing the cluster levels for each node, starting with 0 and going up to K - 1.
		 * @param names             The node labels of the network.
		 * @param earlyConvergence  A boolean indicating whether the genetic algorithm converged before the last iteration.
		 * @param start             The start date and time of the time window network. Can be arbitrarily small if it is only a single network.
		 * @param stop              The end date and time of the time window network. Can be arbitrarily large if it is only a single network.
		 * @param middle            The mid-point date of the time window network. This is used to position the time polarization score on the time axis.
		 */
		public PolarizationResult(double[] maxQ, double[] avgQ, double[] sdQ, double finalMaxQ, int[] memberships, String[] names, boolean earlyConvergence, Date start, Date stop, Date middle) {
			this.maxQ = maxQ;
			this.avgQ = avgQ;
			this.sdQ = sdQ;
			this.finalMaxQ = finalMaxQ;
			this.memberships = memberships;
			this.names = names;
			this.earlyConvergence = earlyConvergence;
			this.start = start;
			this.stop = stop;
			this.middle = middle;
		}

		public Date getStart() {
			return start;
		}

		public Date getStop() {
			return stop;
		}

		public Date getMiddle() {
			return middle;
		}

		public int[] getMemberships() {
			return memberships;
		}

		public String[] getNames() {
			return names;
		}

		public double[] getMaxQ() {
			return maxQ;
		}

		public double[] getAvgQ() {
			return avgQ;
		}

		public double[] getSdQ() {
			return sdQ;
		}

		public double getFinalMaxQ() {
			return finalMaxQ;
		}
	}
}