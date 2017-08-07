package edu.stanford.math.plex4.examples;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import edu.stanford.math.plex4.homology.chain_basis.Simplex;
import edu.stanford.math.plex4.homology.chain_basis.SimplexComparator;
import edu.stanford.math.plex4.streams.impl.ExplicitStream;

/**
 * This class creates a generalization of the cubic-time example shown in
 * "Persistence Algorithm Takes Cubic Time in the Worst Case"
 * by Dmitriy Morozov.
 * 
 * @author Rodrigo Mendoza-Smith
 *
 */
public class MorozovCubicTimeExample {

	/**
	* This function wraps the function that creates a generalization of
	* the filtered simplicial complex shown in "Persistence Algorithm
	* Takes Cubic Time in the Worst Case" by Dmitriy Morozov.
	* <p>
	* The complexity of the resulting simplicial complex
	* is parametrised by the numLevels parameter (set to 3 in the original paper).
	* 
	* @param numLevels Complexity of the complex. The number of elements in the complex
	* grows exponentially in num_levels.
	* @return An object of type ExplicitStream<Simplex> 
	*/
	public static ExplicitStream<Simplex> getMorozovCubicTimeExample(int numLevels) {

		ExplicitStream<Simplex> stream = getStream(numLevels);
		return stream;
	}

	/**
	* This function creates a generalization of the filtered simplicial
	* complex shown in "Persistence Algorithm Takes Cubic Time in the
	* Worst Case" by Dmitriy Morozov.
	* <p>
	* The complexity of the resulting simplicial complex
	* is parametrised by the numLevels parameter (set to 3 in the original paper).
	* 
	* @param numLevels Complexity of the complex. The number of elements in the complex
	* grows exponentially in num_levels.
	* @return An object of type ExplicitStream<Simplex> 
	*/
	private static ExplicitStream<Simplex> getStream(int numLevels){

		// Create List of ArrayLists to store base/fin/c elements.
		// Elements of dimension i are stored in the i-th ArrayList in outer ArrayList.
		ArrayList<ArrayList<Simplex>> baseList = new ArrayList<ArrayList<Simplex>>(3);
		for (int i = 0; i < 3; i++) baseList.add(new ArrayList<Simplex>());

		ArrayList<ArrayList<Simplex>> finList = new ArrayList<ArrayList<Simplex>>(3);
		for (int i = 0; i < 3; i++) finList.add(new ArrayList<Simplex>());

		ArrayList<ArrayList<Simplex>> cList = new ArrayList<ArrayList<Simplex>>(3);
		for (int i = 0; i < 3; i++) cList.add(new ArrayList<Simplex>());

		// Create an array of base vertex labels at level numLevels
		int[] labels = getBaseVertexLabels(numLevels);

		// Create Base and Fin Elements
		insertBaseAndFinElements(baseList, finList, labels, numLevels);

		// Insert C-Elements
		insertCElements(cList, labels);

		// Create auxiliary LinkedList
		List<Simplex> streamList = new ArrayList<Simplex>();

		// Add vertices
		streamList.addAll(baseList.get(0));
		streamList.addAll(finList.get(0));
		streamList.addAll(cList.get(0));

		// Add edges that destroy components
		streamList.addAll(cList.get(1));
		for (int i = 1; i < finList.get(1).size(); i+=2) streamList.add(finList.get(1).get(i));

		// Add left fin edges
		for (int i = finList.get(1).size() - 2; i >= 0; i-=2) streamList.add(finList.get(1).get(i));

		// Add base edges
		streamList.addAll(baseList.get(1));

		// Add base triangles, c triangles, fin triangles
		streamList.addAll(baseList.get(2));
		streamList.addAll(cList.get(2));
		streamList.addAll(finList.get(2));

		// Create ExplicitStream object
		ExplicitStream<Simplex> stream = new ExplicitStream<Simplex>(SimplexComparator.getInstance());

		Iterator<Simplex> iterator = streamList.iterator();
		Simplex simplex;
		int filtrationIndex = 0;
		while(iterator.hasNext()){
			simplex = iterator.next();
			stream.addElement(simplex, filtrationIndex++);
		}

		return stream;

	}

	/**
	* This function populates an object cList of type ArrayList<ArrayList<Simplex>>
	* such that cList.get(i) is an ArrayList<Simplex> object containing elements of
	* dimension i.
	* <p>
	* cList.get(0) contains vertex C.
	* cList.get(1) contains edges that have C as an endpoint.
	* CList.get(2) contains triangles that have an edge composed from adjacent elements
	* in the labels array.
	* 
	* @param cList Complexity of the complex. The number of elements in the complex
	* grows exponentially in numLevels.
	* @param labels An array containing the labels of all base vertices.
	* @return a filtered simplex stream
	*/
	private static void insertCElements(ArrayList<ArrayList<Simplex>> cList, int[] labels){
		cList.get(1).add(new Simplex(new int[]{0, labels[0]}));
		for (int i = 1; i < labels.length; i++){
			cList.get(1).add(new Simplex(new int[]{0, labels[i]}));
			cList.get(2).add(new Simplex(new int[]{labels[i-1], 0, labels[i]}));
		}
		return;
	}

	private static void insertBaseAndFinElements(ArrayList<ArrayList<Simplex>> baseList, 
		ArrayList<ArrayList<Simplex>> finList, int[] labels, int numLevels){

		int windowSize;
		int levelSize;
		int[] levelLabels;
		int j;
		int finVertexLabel = labels.length - 1;
		int[] edge;

		List<Simplex> vertexList = new ArrayList<Simplex>();
		List<int[]> edgeList = new ArrayList<int[]>();
		List<Simplex> triangleList = new ArrayList<Simplex>();

		// We insert elements for each level
		for (int level = 1; level <= numLevels; level++){
			/*
			Get labels for level.
			The int[] labels array is computed for a particular value of numLevels.
			We can also consider the labels array for level <= numLevels.
			For example, in Morozov's paper (numLevels == 3),
			labels = {-2, 4, 2, 1, 7, 3, 5, -1}
			And,
			If level==1, levelLabels = {-2, 1, -1}
			If level==2, levelLabels = {-2, 2, 1, 3, -1}
			If level==3, levelLabels = {-2, 4, 2, 6, 1, 7, 3, 5, -1}
			*/
			windowSize = (int)(Math.pow(2, numLevels - level));
			levelSize = (int)(Math.pow(2, level) + 1);
			j = 0;
			levelLabels = new int[levelSize];
			for (int i = 0; i < labels.length; i = i + windowSize){
				levelLabels[j++] = labels[i];
			}

			// Level 1 is a special case
			if (level == 1){

				// Add vertices A=-2, B=-1, C=0, 1
				baseList.get(0).add(new Simplex(new int[]{levelLabels[0]}));
				baseList.get(0).add(new Simplex(new int[]{levelLabels[2]}));
				baseList.get(0).add(new Simplex(new int[]{0}));
				baseList.get(0).add(new Simplex(new int[]{levelLabels[1]}));

				// Add edges A-1, 1-B and corresponding fin triangles
				for (int i = 1; i < 3; i++){
					baseList.get(1).add(new Simplex(new int[]{levelLabels[i-1], levelLabels[i]}));

					// add fin elements corresponding to edge
					finList.get(0).add(new Simplex(new int[]{finVertexLabel}));
					finList.get(1).add(new Simplex(new int[]{levelLabels[i-1], finVertexLabel}));
					finList.get(1).add(new Simplex(new int[]{finVertexLabel, levelLabels[i]}));
					finList.get(2).add(new Simplex(new int[]{levelLabels[i-1], finVertexLabel, levelLabels[i]}));
					finVertexLabel++;
				}

				// Add triangle A-1-B
				baseList.get(2).add(new Simplex(new int[]{levelLabels[0], levelLabels[1], levelLabels[2]}));

			}else{ // For level >=2

				// Create vertices
				// Don't include endpoints of levelLabels since they were added in case level==1
				for (int i = 1; i < levelLabels.length - 1; i+=2)
					if (levelLabels[i] != 1) vertexList.add(new Simplex(new int[]{levelLabels[i]}));
				while (!vertexList.isEmpty()){
					baseList.get(0).add(vertexList.remove(0));
					baseList.get(0).add(vertexList.remove(vertexList.size() - 1));
				}

				// Create edges
				for (int i = 1; i < levelLabels.length; i++)
					edgeList.add(new int[]{levelLabels[i-1], levelLabels[i]});
				while (!edgeList.isEmpty()){
					// Get first edge and create/add its fin elements
					edge = edgeList.remove(0);
					baseList.get(1).add(new Simplex(new int[]{edge[0], edge[1]}));

					finList.get(0).add(new Simplex(new int[]{finVertexLabel}));
					finList.get(1).add(new Simplex(new int[]{edge[0], finVertexLabel}));
					finList.get(1).add(new Simplex(new int[]{finVertexLabel, edge[1]}));
					finList.get(2).add(new Simplex(new int[]{edge[0], finVertexLabel, edge[1]}));
					finVertexLabel++;

					// Get last edge and create/add its fin elements
					edge = edgeList.remove(edgeList.size() - 1);
					baseList.get(1).add(new Simplex(new int[]{edge[0], edge[1]}));

					finList.get(0).add(new Simplex(new int[]{finVertexLabel}));
					finList.get(1).add(new Simplex(new int[]{edge[0], finVertexLabel}));
					finList.get(1).add(new Simplex(new int[]{finVertexLabel, edge[1]}));
					finList.get(2).add(new Simplex(new int[]{edge[0], finVertexLabel, edge[1]}));
					finVertexLabel++;
				}

				// Create triangles
				for (int i = 2; i < levelLabels.length; i = i + 2)
					triangleList.add(new Simplex(new int[]{levelLabels[i-2], levelLabels[i-1], levelLabels[i]}));
				while (!triangleList.isEmpty()){
					baseList.get(2).add(triangleList.remove(0));
					baseList.get(2).add(triangleList.remove(triangleList.size() - 1));
				}

			}
		}
		// We add edge A-B and reverse the list.
		baseList.get(1).add(new Simplex(new int[]{labels[0], labels[labels.length-1]}));
		Collections.reverse(baseList.get(1));

		return;
	}

	/**
	* This function creates an int[] array of size 2^{numLevels}+2 containing a list
	* of base vertices ordered from vertex A=-2 to vertex B=-1. For the complex in 
	* Morozov's paper (numLevels = 3) the list is:
	* <p>
	* int[] labels = {-2, 4, 2, 6, 1, 7, 3, 5, -1}
	* <p>
	* If numLevels == 4, then,
	* int[] labels = {-2, 8, 4, 10, 2, 12, 6, 14, 1, 15, 7, 13, 3, 11, 5, 9, -1} 
	* 
	* @param numLevels Complexity of the complex. Resulting array has length
	* 2^{numLevels}+2.
	* @return An array labels of type int[] containing the list of base vertices
	* ordered from vertex A=-2 to vertex B=-1.
	*/
	private static int[] getBaseVertexLabels(int numLevels) {
		// Number of base vertices excluding A, B, and C.
		int numVertices = (int)(Math.pow(2, numLevels)) - 1;

		// We allocate space in "labels" for A and B as well.
		int[] labels = new int[numVertices + 2];

		// Initialize "labels"
		for (int i = 0; i < labels.length; i++) labels[i] = 0;

		// Create vector of base vertices ordered such that elements
		// can be computed recursively.
		int windowSize;
		int leftSentinel;
		int rightSentinel;
		int label = 1;
		for (int level = 1; level <= numLevels; level++){
			windowSize = (int)(Math.pow(2, numLevels - level));
			leftSentinel = windowSize;
			rightSentinel = numVertices + 1 - windowSize;
			while (leftSentinel <= rightSentinel){
				labels[leftSentinel] = label++;
				if (level > 1){
					labels[rightSentinel] = label++;
				}
				leftSentinel += 2*windowSize;
				rightSentinel -= 2*windowSize;
			}
		}
		labels[0] = -2;
		labels[labels.length-1] = -1;

		return labels;

	}

}

