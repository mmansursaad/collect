package com.jed.optima.entities.javarosa.spec

import org.javarosa.xform.parse.XFormParser

class UnrecognizedEntityVersionException(val entityVersion: String) : XFormParser.ParseException()
