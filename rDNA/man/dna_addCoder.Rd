% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/rDNA.R
\name{dna_addCoder}
\alias{dna_addCoder}
\title{Add a new coder to the DNA database}
\usage{
dna_addCoder(connection, name, color, addDocuments = TRUE,
  editDocuments = TRUE, deleteDocuments = TRUE,
  importDocuments = TRUE, viewOthersDocuments = TRUE,
  editOthersDocuments = TRUE, addStatements = TRUE,
  viewOthersStatements = TRUE, editOthersStatements = TRUE,
  editCoders = TRUE, editStatementTypes = TRUE, editRegex = TRUE,
  verbose = TRUE)
}
\arguments{
\item{connection}{A \code{dna_connection} object created by the
\code{dna_connection} function.}

\item{name}{A character object indicating the name of the new coder.}

\item{color}{A character object indicating the color in which the new coder
should be displayed in the graphical user interface. The color must be
supplied as a hexadecimal string, for example \code{"#FFFF00"} for yellow.}

\item{addDocuments}{Logical: should the coder have the permission to add new
documents via the graphical user interface?}

\item{editDocuments}{Logical: should the coder have permission to edit the
meta-data of documents in the graphical user interface?}

\item{deleteDocuments}{Logical: should the coder have permission to delete
documents from the database in the graphical user interface?}

\item{importDocuments}{Logical: should the coder have permission to import
documents into the database via the graphical user interface?}

\item{viewOthersDocuments}{Logical: should the coder have permission to view
the documents that were added by other coders?}

\item{editOthersDocuments}{Logical: should the coder have permission to edit
the meta-data of documents added by other coders?}

\item{addStatements}{Logical: should the coder have permission to add new
statements to the database?}

\item{viewOthersStatements}{Logical: should the coder have permission to view
the statements in the graphical user interface that were added by other
coders?}

\item{editOthersStatements}{Logical: should the coder have permission to edit
the statements in the graphical user interface that were added by other
coders?}

\item{editCoders}{Logical: should the coder have permission to add, remove,
or edit coders in the graphical user interface?}

\item{editStatementTypes}{Logical: should the coder have permission to add,
remove, or edit statement types in the graphical user interface?}

\item{editRegex}{Logical: should the coder have permission to add, remove, or
edit regular expressions in the graphical user interface?}

\item{verbose}{Print details?}
}
\description{
Add a new coder to the DNA database.
}
\details{
The \code{dna_addCoder} function can add a new coder to an existing DNA
database. The user supplies a \link{dna_connection} object, the name of the
new coder, the color used to display the coder in the graphical user
interface, as well as various permissions of the coder.
}
\author{
Philip Leifeld
}
