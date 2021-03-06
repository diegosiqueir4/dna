% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/rDNA.R
\name{dna_plotDendro}
\alias{dna_plotDendro}
\title{Plots a dendrogram from dna_cluster objects}
\usage{
dna_plotDendro(clust, shape = "elbows", activity = FALSE,
  leaf_colors = "attribute1", branch_color = "#636363",
  colors = "identity", custom_colors = character(),
  leaf_ends = character(), custom_shapes = character(),
  ends_alpha = 1, ends_size = 3, rectangles = character(),
  leaf_linetype = "a", branch_linetype = "b", line_width = 1,
  line_alpha = 1, font_size = 12, theme = "bw", truncate = 30,
  leaf_labels = "ticks", circular = FALSE, show_legend = TRUE, ...)
}
\arguments{
\item{clust}{A \code{dna_cluster} object created by the \link{dna_cluster}
function.}

\item{shape}{The shape of the dendrogram. Available options are \code{elbows},
\code{link}, \code{diagonal}, \code{arc}, and \code{fan}.}

\item{activity}{Should activity of variable in \link{dna_cluster} be used to
determine size of leaf_ends (logical). Activity means the number of
statements which remained after duplicates were removed.}

\item{leaf_colors}{Determines which data is used to color the leafs of the
dendrogram. Can be either \code{attribute1}, \code{attribute2}or \code{group}. Set to
\code{character()} leafs-lines should not be colored.}

\item{branch_color}{Provide one color in which all branches are colored.}

\item{colors}{There are three options from where to derive the colors in
the plot: (1.) \code{identity} tries to use the names of variables as colors
(e.g., if you retrieved the names as attribute from DNA), fails if names
are not plottable colors; (2.) \code{manual} provide colors via
custom_colors; (3.) \code{brewer} automatically select nice colors from a
\code{RColorBrewer} palette (palettes can be set in custom_colors,
defaults to \code{Set3}).}

\item{custom_colors}{Either provide enough colors to manually set the
colors in the plot (if \code{colors = "manual"}) or select a palette from
\code{RColorBrewer} (if \code{colors = "brewer"}).}

\item{leaf_ends}{Determines which data is used to color the leaf_ends of the
dendrogram. Can be either \code{attribute1}, \code{attribute2} or \code{group}. Set to
\code{character()} if no line ends should be displayed.}

\item{custom_shapes}{If shapes are provided, those are used for leaf_ends
instead of the standard ones. Available shapes range from 0:25 and 32:127.}

\item{ends_alpha}{Alpha of all leaf_ends.}

\item{ends_size}{If \code{activity = FALSE}, the size of the lineend symbols
can be set to one size for the whole plot.}

\item{rectangles}{If a color is provided, this will draw rectangles in given
color around the groups.}

\item{leaf_linetype, branch_linetype}{Determines which lines are used for
leafs and branches. Takes \code{a} for straight line or \code{b} for dotted line.}

\item{line_width}{Width of all lines.}

\item{line_alpha}{Alpha of all lines.}

\item{font_size}{Set the font size for the entire plot.}

\item{theme}{See themes in \code{ggplot2}. The theme \code{bw} was customized to
look best with dendrograms. Leave empty to use standard ggplot theme.
customize the theme by adding \code{+ theme_*} after this function...}

\item{truncate}{Sets the number of characters to which labels should be
truncated. Value \code{Inf} turns off truncation.}

\item{leaf_labels}{Either \code{ticks} to display the labels as axis ticks or
\code{node} to label nodes directly. Node labels are also take the same color
as the leaf the label.}

\item{circular}{Logical. Should the layout be transformed to a circular
representation.}

\item{show_legend}{Logical. Should a legend be displayed.}

\item{...}{Not used. If you want to add more plot options use \code{+} and
the ggplot2 logic (see example).}
}
\description{
Plots a dendrogram from objects derived via \link{dna_cluster}.
}
\details{
This function is a convenience wrapper for several different dendrogram
types, which can be plotted using the \pkg{ggraph} package.
}
\examples{
\dontrun{
dna_init()
conn <- dna_connection(dna_sample())
clust <- dna_cluster(conn)
dend <- dna_plotDendro(clust)

# Flip plot with ggplot2 command
library("ggplot2")
dend + coord_flip()
}

}
\author{
Johannes B. Gruber
}
