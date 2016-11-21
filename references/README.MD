
[TOC]
# MaxSAT by improved instance-specific algorithm configuration
*Carlos Ansótegui, Joel Gabàs, Yuri Malitsky, Meinolf Sellmann*
*Artificial Intelligence. Volume 235, June 2016, Pages 26–39*
**ISAC++:** a portfolio tuning approach which generalizes both tuning of individual solvers as well as combining multiple solvers into one solver portfolio.
**Motivation:**there is oftentimes no single solver taht performs best on every single instance family.
##  MaxSAT
...
## Related work
two main approaches
- branch-and-bound-based algorithms (dominate on random and some families of crafted instances)
- SAT-based solvers (dominate on industrial and some crafted instances)
## Meta algorithms
- **Algorithm portfolios**
    + SATzilla-2007 (use regression function to predict the performance of every solver in the given set of solvers based on the features of an instance)
    + 3S (based on KNN)
    + SATzilla2012 (uses a low-bias machine learning approach, that relies on cost-sensitive decision forests and voting)
    + CSHC (based on cost-sensitive hierarchical clustering of training instances.)
- **Algorithm tuning**
Portfolio approaches are very powerful in practice, but there are many domains that do not have a plethora of diverse high-performance solvers.Configure the parameters of one solver to gain the most benefit on a particular benchmark.
Challenge:non-linear interactions between parameters of sophisticated state-of-the-art algorithms makes manual tuning very difficult.
Two most techniques:
    + model-based: SMAC
    + population-based: GGA
## Instance-Specific Algorithm Configuration(ISAC) solver
1. compute features for each training instance and normalize these
2. cluster the training instances as represented by their normalized features(g-means: assumes a good cluster to be Gaussian distributed, and iteratively splits clusters in two until they all pass the Anderson–Darling statistic, a statistical test for “Gaussianness.”)
3. for each cluster, use an instance-oblivious tuner to compute a good parametrization that work well for all instances in the same cluster
4. given an instance, compute the features and normalize these features in the same way we normalized the training instances
5. computes the cluster nearest to the given instance
![ISAC](./imgs/ISAC\ algorithm.png)
## ISAC++ 
weakness of algorithm tuning: the selection of a parametrization out of a very large and possibly even infinite pool of possible parameter settings.
weakness of algorithm portfolios: small set of solvers.
ISAC overcome this problem by clustering the training instances.
**ISAC++ solver**
1. - 4. the same as ISAC 1-4
5. uses an algorithm selector to determine which parametrization should be used for the given instance. (use GGA to tune instance-obliviously, and CSHC for algorithm selection).
## Experimental study
- on SAT benchmarks(features: the number of variables, number of clauses, proportion of positive to negative literals, the number of clauses a variable appears in on average)
- on MaxSAT benchmarks(features: the percentage of clauses that are soft, and the statistics of the distribution of weights: mean, minimum, maximum, and standard deviation and SAT features)


# Greedy or Not? Best Improving versus First Improving Stochastic Local Search for MAXSAT
*D Whitley, AE Howe, D Hains - AAAI, 2013 - Citeseer*
**Motivation:** Studies indicate that first improving local search was just as effective as “greedy” best improving local search. Their studies only used SAT problems with a small number of variables. Do their conclusions hold for larger problems and industrial MAXSAT applications as well as state of the art SLS algorithms?

**Best improving:** “Best improving” local search for MAXSAT identifies all of the improving moves in the Hamming distance 1 neighborhood, then uses the best improving move to update the incumbent solution.
**First improving:** “first improving” local search arbitrarily selects the first available improving move to update the incumbent solution.

## Runtime costs for move selection
Existing best improving SLS algorithms have runtime costs that can be one or two orders of magnitude slower than using first improving SLS algorithms. To support a fair comparison, introducing a new high fidelity approximate best improving move selection method.

## Greedy or not?
**Two stages:** The first stage occurs as the search is locating the first local optimum. In the second stage, a heuristic is often used to decide which bits to flip.
*First, are the local optima reached using best improving moves significantly better than for first improving moves?*
+ the industrial problems and random problems display different patterns.
+ Best improving is significantly better on industrial instances.
+ First improving is significantly better on uniform random problems.

*How much does this starting point matter to voerall performance?*
The advantage of the best move selection upon arrival at the first local optimum is lost during the remainder of search.

*In the second stage of search, which is better?*
Thus, using best improving moves in the first stage can significantly degrade performance overall even when using first improving moves in the second stage. Which strategy is best for the second stage? The results are not clear cut. 

## Explaining the results
critical variables.