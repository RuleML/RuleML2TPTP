RuleML2TPTP
=============================

*This is an easy-to-use translator utilizing XSLT 2.0 between two rule languages.*

### Notice

This project was originally developed on [EdmonL/RuleML2TPTP](https://github.com/EdmonL/RuleML2TPTP), but now the development work has been transferred to this repository. The old repository [EdmonL/RuleML2TPTP](https://github.com/EdmonL/RuleML2TPTP) is kept only for reference (like a frozen branch or a tag). The entry page for the old repository is [here](http://edmonl.github.io/RuleML2TPTP/).

### Introduction

[RuleML](http://wiki.ruleml.org) is a knowledge representation language developed by the non-profit organization RuleML Inc. RuleML is being used for sharing rule bases in XML and publishing them on the Web. It has broad coverage and is defined as an extensible family of sublanguages across various rule logics and platforms. RuleML consists of [Deliberation RuleML](http://wiki.ruleml.org/index.php/Specification_of_Deliberation_RuleML) and [Reaction RuleML](http://wiki.ruleml.org/index.php/Specification_of_Reaction_RuleML). 

This project is aimed at implementing an [XSLT 2.0](http://www.w3.org/TR/xslt20/) translator to convert [Deliberation RuleML 1.01](http://wiki.ruleml.org/index.php/Specification_of_Deliberation_RuleML_1.01) in XML format to an equivalent representation in a subset of the (FOF) [TPTP](http://www.cs.miami.edu/~tptp/) language.

### Getting Started

1. This project is built using [maven](http://maven.apache.org/).
2. Translate a RuleML file by calling "java -jar /path/to/ruleml2tptp.jar &lt;source&gt; -o &lt;output&gt;". Use option "-h" for a brief usage.
3. This project needs JDK 1.7 or higher to compile the Java code.

### RuleML Examples

Here are some good RuleML examples from [RuleML wiki](http://wiki.ruleml.org/index.php/Specification_of_Deliberation_RuleML_1.01#Examples) to try with this project.

