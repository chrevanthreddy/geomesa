/***********************************************************************
 * Copyright (c) 2013-2019 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/

package org.locationtech.geomesa.convert2.validators

import org.junit.runner.RunWith
import org.locationtech.geomesa.utils.geotools.SimpleFeatureTypes
import org.opengis.feature.simple.{SimpleFeature, SimpleFeatureType}
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SimpleFeatureValidatorTest extends Specification {

  val sft = SimpleFeatureTypes.createType("foo", "*geom:Point:srid=4326")

  "SimpleFeatureValidator" should {
    "allow custom SPI loading" in {
      val custom = SimpleFeatureValidator(sft, Seq("custom"))
      custom must not(beNull)
      custom.validate(null) must beNull
      SimpleFeatureValidatorTest.errors.set("foo")
      try {
        custom.validate(null) mustEqual "foo"
      } finally {
        SimpleFeatureValidatorTest.errors.remove()
      }
    }
    "allow custom SPI loading with options" in {
      val custom = SimpleFeatureValidator(sft, Seq("custom(foo,bar,baz)"))
      custom must not(beNull)
      custom.validate(null) mustEqual "foo,bar,baz"
      SimpleFeatureValidatorTest.errors.set("foo")
      try {
        custom.validate(null) mustEqual "foo"
      } finally {
        SimpleFeatureValidatorTest.errors.remove()
      }
    }
    "allow custom SPI loading of deprecated v1 validators" in {
      val custom = SimpleFeatureValidator(sft, Seq("custom-v1"))
      custom must not(beNull)
      custom.validate(null) must beNull
      SimpleFeatureValidatorTest.errors.set("foo")
      try {
        custom.validate(null) mustEqual "foo"
      } finally {
        SimpleFeatureValidatorTest.errors.remove()
      }
    }
    "allow custom SPI loading of deprecated v1 validators with options" in {
      val custom = SimpleFeatureValidator(sft, Seq("custom-v1(foo,bar,baz)"))
      custom must not(beNull)
      custom.validate(null) mustEqual "foo,bar,baz"
      SimpleFeatureValidatorTest.errors.set("foo")
      try {
        custom.validate(null) mustEqual "foo"
      } finally {
        SimpleFeatureValidatorTest.errors.remove()
      }
    }
  }
}

object SimpleFeatureValidatorTest {

  val errors = new ThreadLocal[String]()

  class CustomValidator(val config: Option[String]) extends SimpleFeatureValidator {
    override def validate(sf: SimpleFeature): String = Option(errors.get).orElse(config).orNull
  }

  // note: registered in
  // src/test/resources/META-INF/services/org.locationtech.geomesa.convert2.validators.SimpleFeatureValidatorFactory
  class CustomValidatorFactory extends SimpleFeatureValidatorFactory {
    override def name: String = "custom"
    override def apply(sft: SimpleFeatureType, config: Option[String]): SimpleFeatureValidator =
      new CustomValidator(config)
  }

  // v1 deprecated validators for back compatibility tests
  class CustomValidatorV1(val config: Option[String])
      extends org.locationtech.geomesa.convert.SimpleFeatureValidator.Validator {
    override def validate(sf: SimpleFeature): String = Option(errors.get).orElse(config).orNull
  }

  // note: registered in
  // src/test/resources/META-INF/services/org.locationtech.geomesa.convert.SimpleFeatureValidator$ValidatorFactory
  class CustomValidatorFactoryV1 extends org.locationtech.geomesa.convert.SimpleFeatureValidator.ValidatorFactory {
    override def name: String = "custom-v1"
    override def validator(sft: SimpleFeatureType, config: Option[String]): org.locationtech.geomesa.convert.SimpleFeatureValidator.Validator
    = new CustomValidatorV1(config)
  }
}
