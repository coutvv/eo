<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2016-2024 Objectionary.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
-->
<!--
Raise an error if warnings are found within program
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="fail-on-warnings" version="2.0">
  <xsl:output encoding="UTF-8" method="xml"/>
  <xsl:template match="/program/errors/error[@severity='warning']">
    <xsl:message terminate="yes">
      <xsl:text>Warnings identified:</xsl:text>
      <xsl:value-of select="'&#10;'"/>
      <xsl:for-each select="/program/errors/error[@severity='warning']">
        <xsl:text>  </xsl:text>
        <xsl:value-of select="/program/@source"/>
        <xsl:if test="@line">
          <xsl:text>:</xsl:text>
          <xsl:value-of select="@line"/>
        </xsl:if>
        <xsl:text> </xsl:text>
        <xsl:value-of select="text()"/>
        <xsl:text> (</xsl:text>
        <xsl:value-of select="@check"/>
        <xsl:text>)</xsl:text>
        <xsl:value-of select="'&#10;'"/>
      </xsl:for-each>
    </xsl:message>
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>