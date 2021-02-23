# Library Philosophy

This file is for answering questions around some of this library's design. This can also
serve as a reference point for why certain parts of the library are packaged a certain
way, or explaining choices made during creation, so that it can guide future decisions.

And if, down the line, a better approach is proposed, at least there's a paper trail for
the decisions already made so that more *educated* decisions can be made.


### What is the primary goal of this project?

To do Doom-related things at the command-line, mostly data-related, not game-related.


### [WadScript] Why is there no WADREPLACE() built-in for replacing WAD entries?

"Replacing" an entry is essentially removing and adding at the same index, but you can
remove an entry two ways: Deletion of just the entry, or deletion of the entry plus
data purge (and reshuffling of other entry offsets).

Since the user knows the end result of the WAD data manipulation, it's better to allow 
them to make a decision based around a potentially expensive set of calls.

The `Wad.replaceEntry()` method in the Doom library is a tad more specialized 
for its expected use case, but this approach is the more correct one in terms of user 
control and WadScript's use case.
