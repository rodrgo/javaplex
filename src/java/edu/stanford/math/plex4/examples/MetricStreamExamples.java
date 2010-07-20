package edu.stanford.math.plex4.examples;

import edu.stanford.math.plex4.homology.chain_basis.Simplex;
import edu.stanford.math.plex4.homology.streams.impl.LazyWitnessStream;
import edu.stanford.math.plex4.homology.streams.interfaces.AbstractFilteredStream;
import edu.stanford.math.plex4.math.metric.impl.EuclideanMetricSpace;
import edu.stanford.math.plex4.math.metric.interfaces.SearchableFiniteMetricSpace;
import edu.stanford.math.plex4.math.metric.landmark.LandmarkSelector;
import edu.stanford.math.plex4.math.metric.landmark.MaxMinLandmarkSelector;

public class MetricStreamExamples {
	
	public static AbstractFilteredStream<Simplex> getLazyWitnessTorus(int points, int landmarkPoints, double r, double R, double maxFiltrationValue, int numDivisions) {
		SearchableFiniteMetricSpace<double[]> metricSpace = new EuclideanMetricSpace(PointCloudExamples.getRandomTorusPoints(points, r, R));
		
		LandmarkSelector<double[]> selector = new MaxMinLandmarkSelector<double[]>(metricSpace, landmarkPoints);
		LazyWitnessStream<double[]> stream = new LazyWitnessStream<double[]>(metricSpace, selector, 3, maxFiltrationValue, numDivisions);
		stream.finalizeStream();
		
		return stream;
	}
	
	public static AbstractFilteredStream<Simplex> getLazyWitnessTorus() {
		return getLazyWitnessTorus(1000, 300, 1, 2, 0.4, 100);
	}
	
	public static AbstractFilteredStream<Simplex> getLazyWitnessSphere(int points, int landmarkPoints, int dimension, double maxFiltrationValue, int numDivisions) {		
		SearchableFiniteMetricSpace<double[]> metricSpace = new EuclideanMetricSpace(PointCloudExamples.getRandomSpherePoints(points, dimension));
		
		LandmarkSelector<double[]> selector = new MaxMinLandmarkSelector<double[]>(metricSpace, landmarkPoints);
		LazyWitnessStream<double[]> stream = new LazyWitnessStream<double[]>(metricSpace, selector, dimension + 1, maxFiltrationValue, numDivisions);
		stream.finalizeStream();
		
		return stream;
	}
	
	public static AbstractFilteredStream<Simplex> getLazyWitness2Sphere() {
		return getLazyWitnessSphere(1000, 100, 2, 0.2, 100);
	}
}