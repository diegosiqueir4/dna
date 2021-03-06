% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/rDNA.R
\name{dna_scale1dbin}
\alias{dna_scale1dbin}
\title{One-dimensional binary scaling from a DNA connection}
\usage{
dna_scale1dbin(connection, variable1 = "organization",
  variable2 = "concept", qualifier = "agreement", threshold = NULL,
  theta_constraints = NULL, mcmc_iterations = 20000,
  mcmc_burnin = 1000, mcmc_thin = 10, mcmc_normalize = FALSE,
  theta_start = NA, alpha_start = NA, beta_start = NA,
  theta_prior_mean = 0, theta_prior_variance = 1,
  alpha_beta_prior_mean = 0, alpha_beta_prior_variance = 0.25,
  store_variables = "both", drop_constant_concepts = FALSE,
  drop_min_actors = 1, drop_min_concepts = 2, verbose = TRUE,
  seed = 12345, ...)
}
\arguments{
\item{connection}{A \code{dna_connection} object created by the
\link{dna_connection} function.}

\item{variable1}{The first variable for the scaling construction (see
\link{dna_network}). Defaults to \code{"organization"}.}

\item{variable2}{The second variable for the scaling construction (see
\link{dna_network}). Defaults to \code{"concept"}.}

\item{qualifier}{The qualifier variable for the scaling construction (see
\link{dna_network}). Defaults to \code{"agreement"}.}

\item{threshold}{Numeric value that specifies when a mixed position can be
considered as agreement or disagreement. If e.g. one actor has 60 percent
of agreeing and 40 percent of disagreeing statements towards a concept, a
\code{threshold} of 0.51 will recode the actor position on this concept as
"agreement". The same accounts also for disagreeing statements. If one
actor has 60 percent of disagreeing and 40 percent of agreeing statements,
a \code{threshold} of 0.51 will recode the actor position on this concept
as "disagreement". All values in between the \code{threshold} (e.g., 55
percent agreement and 45 percent of disagreement and a threshold of 0.6)
will be recoded as \code{NA}. If is set to \code{NULL}, all "mixed"
positions of actors will be recoded as \code{NA}. Must be strictly
positive.}

\item{theta_constraints}{A list specifying the constraints on the actor
parameter. Three forms of constraints are possible:
\code{actorname = value}, which will constrain an actor to be equal to the
specified value (e.g. \code{0}), \code{actorname = "+"}, which will
constrain the actor to be positively scaled and \code{actorname = "-"},
which will constrain the actor to be negatively scaled (see example).}

\item{mcmc_iterations}{The number of iterations for the sampler.}

\item{mcmc_burnin}{The number of burn-in iterations for the sampler.}

\item{mcmc_thin}{The thinning interval for the sampler. Iterations must be
divisible by the thinning interval.}

\item{mcmc_normalize}{Logical. Should the MCMC output be normalized? If
\code{TRUE}, samples are normalized to a mean of \code{0} and a standard
deviation of \code{1}.}

\item{theta_start}{The \code{starting values} for the actor parameters. Can
either be a scalar or a column vector with as many elements as the number
of actors included in the scaling. If set to the default \code{NA},
\code{starting values} will be set according to an eigenvalue-eigenvector
decomposition of the actor agreement score.}

\item{alpha_start}{The \code{starting values} for the concept difficulty
parameters. Can either be a scalar or a column vector with as many
elements as the number of actors included in the scaling. If set to the
default \code{NA}, \code{starting values} will be set according to a
series of probit regressions that condition the starting values of the
difficulty parameters.}

\item{beta_start}{The \code{starting values} for the concept discrimination
parameters. Can either be a scalar or a column vector with as many
elements as the number of actors included in the scaling. If set to the
default \code{NA}, \code{starting values} will be set according to a
series of probit regressions that condition the \code{starting values} of
the discrimination parameters.}

\item{theta_prior_mean}{A scalar value specifying the prior mean of the
actor parameters.}

\item{theta_prior_variance}{A scalar value specifying the prior inverse
variances of the actor parameters.}

\item{alpha_beta_prior_mean}{Mean of the difficulty and discrimination
parameters. Can either be a scalar or a 2-vector. If a scalar, both means
will be set according to the specified value.}

\item{alpha_beta_prior_variance}{Inverse variance of the difficulty and
discrimination parameters. Can either be a scalar or a 2-vector. If a
scalar, both means will be set according to the specified value.}

\item{store_variables}{A character vector indicating which variables should
be stored from the scaling. Can either take the value of the character
vector indicated in \code{variable1} or \code{variable2} or \code{"both"}
to store both variables. Note that saving both variables can impact the
speed of the scaling. Defaults to \code{"both"}.}

\item{drop_constant_concepts}{Logical. Should concepts that have no
variation be deleted before the scaling? Defaults to \code{FALSE}.}

\item{drop_min_actors}{A numeric value specifying the minimum number of
concepts actors should have mentioned to be included in the scaling.
Defaults to \code{1}.}

\item{drop_min_concepts}{A numeric value specifying the minimum number a
concept should have been jointly mentioned by actors. Defaults to \code{2}.}

\item{verbose}{A boolean or numeric value indicating whether the iterations
of the scaling should be printed to the R console. If set to a numeric
value, every \code{verboseth} iteration will be printed. If set to
\code{TRUE}, \code{verbose} will print the total of iterations and burn-in
divided by \code{10}.}

\item{seed}{The random seed for the scaling.}

\item{...}{Additional arguments passed to \link{dna_network}. Actors can
e.g. be removed with the \code{excludeValues} arguments. The scaling can
also be applied to a specific time slice by using \code{start.date} and
\code{stop.date}.}
}
\description{
Scale ideological positions of two variables (e.g., organizations and
concepts) from a DNA connection by using Markov Chain Monte Carlo for binary
one-dimensional Item Response Theory. This is one of the four scaling
functions. For one-dimensional ordinal scaling, see \link{dna_scale1dord},
for two-dimensional binary scaling, see \link{dna_scale2dbin} and for
two-dimensional ordinal scaling \link{dna_scale2dord}.
}
\details{
This function is a convenience wrapper for the \link[MCMCpack]{MCMCirt1d}
function. Using Markov Chain Monte Carlo (MCMC), \code{dna_scale1dbin}
generates a sample from the posterior distribution using standard Gibbs
sampling. For the model form and further help for the scaling arguments, see
\link[MCMCpack]{MCMCirt1d}.

As in a two-mode network in \link{dna_network}, two variables have to be
provided for the scaling. The first variable corresponds to the rows of a
two-mode network and usually entails actors (e.g., \code{"organizations"}),
while the second variable is equal to the columns of a two-mode network,
typically expressed by \code{"concepts"}. The \code{dna_scale} functions
use \code{"actors"} and \code{"concepts"} as synonyms for \code{variable1}
and \code{variable2}. However, the scaling is not restricted to
\code{"actors"} and \code{"concepts"} but depends on what you provide in
\code{variable1} or \code{variable2}.

For a binary qualifier, \code{dna_scale1dbin} internally uses the
\code{combine} qualifier aggregation and then recodes the values into
\code{0} for disagreement, \code{1} for agreement and \code{NA} for mixed
positions and non-mentions of concepts. Integer qualifiers are also recoded
into \code{0} and \code{1} by rescaling the qualifier values between
\code{0} and \code{1}. You can further relax the recoding of \code{NA} values by setting a
\code{threshold} which lets you decide at which percentage of agreement and
disagreement an actor position on a concept can be considered as
agreement/disagreement or mixed position.

The argument \code{drop_min_actors} excludes actors with only a limited
number of concepts used. Limited participation of actors in a debate can
impact the scaling of the ideal points, as actors with only few mentions of
concepts convey limited information on their ideological position. The same
can also be done for concepts with the argument \code{drop_min_concepts}.
Concepts that have been rarely mentioned do not strongly discriminate the
ideological positions of actors and can, therefore, impact the accuracy of
the scaling. Reducing the number of actors of concepts to be scaled hence
improves the precision of the ideological positions for both variables and
the scaling itself. Another possibility to reduce the number of concepts is
to use \code{drop_constant_concepts}, which will reduce concepts not having
any variation in the agreement/disagreement structure of actors. This means
that all concepts will be dropped which have only agreeing or disagreeing
statements.

As \code{dna_scale1dbin} implements a Bayesian Item Response Theory
approach, \code{priors} and \code{starting values} can be set on the actor
and concept parameters. Changing the default \code{prior} values can often
help you to achieve better results. Constraints on the actor parameters can
also be specified to help identifying the model and to indicate in which
direction ideological positions of actors and concepts run. The returned
MCMC output can also be post-processed by normalizing the samples for each
iteration with \code{mcmc_normalize}. Normalization can be a sufficient
way of identifying one-dimensional ideal point models.

To plot the resulting ideal points of actors and concepts, you can use the
\link{dna_plotScale} function. To assess if the returned MCMC chain has
converged to its stationary distribution, please use
\link{dna_convergenceScale}. The evaluation of convergence is essential to
report conclusions based on accurate parameter estimates. Achieving chain
convergence often requires setting the iterations of the MCMC chain to
several million.
}
\examples{
\dontrun{
dna_init()
conn <- dna_connection(dna_sample())
dna_scale <- dna_scale1dbin(
  conn,
  variable1 = "organization",
  variable2 = "concept",
  qualifier = "agreement",
  threshold = 0.51,
  theta_constraints = list(
    `National Petrochemical & Refiners Association` = "+",
    `Alliance to Save Energy` = "-"),
  mcmc_iterations = 20000,
  mcmc_burnin = 2000,
  mcmc_thin = 10,
  mcmc_normalize = TRUE,
  theta_prior_mean = 0,
  theta_prior_variance = 1,
  alpha_beta_prior_mean = 0,
  alpha_beta_prior_variance = 0.25,
  store_variables = "both",
  drop_constant_concepts = FALSE,
  drop_min_actors = 1,
  verbose = TRUE,
  seed = 12345
)
}

}
\author{
Tim Henrichsen, Johannes B. Gruber
}
