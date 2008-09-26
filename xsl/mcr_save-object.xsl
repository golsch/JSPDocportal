<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- ============================================== -->
<!-- $Revision: 1.1 $ $Date: 2006-04-06 09:36:37 $ -->
<!-- ============================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
> 

<xsl:output method="xml" encoding="UTF-8"/>

<xsl:variable name="newline">
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:attribute-set name="tag">
  <xsl:attribute name="class">
    <xsl:value-of select="./@class" />
  </xsl:attribute>
  <xsl:attribute name="heritable">
    <xsl:value-of select="./@heritable" />
  </xsl:attribute>
  <xsl:attribute name="notinherit">
    <xsl:value-of select="./@notinherit" />
  </xsl:attribute>
  <xsl:attribute name="parasearch">
    <xsl:value-of select="./@parasearch" />
  </xsl:attribute>
  <xsl:attribute name="textsearch">
    <xsl:value-of select="./@textsearch" />
  </xsl:attribute>
</xsl:attribute-set>

<xsl:template match="/">
  <mycoreobject>
    <xsl:copy-of select="mycoreobject/@ID"/>
    <xsl:copy-of select="mycoreobject/@label"/>
    <xsl:copy-of select="mycoreobject/@xsi:noNamespaceSchemaLocation"/>
    <structure>
      <xsl:copy-of select="mycoreobject/structure/parents"/>
      <xsl:copy-of select="mycoreobject/structure/childs"/>
    </structure>
    <metadata xml:lang="de">
    <xsl:for-each select="mycoreobject/metadata/*">
      <xsl:for-each select="." >
        <xsl:if test="./*/@inherited = '0'">
          <xsl:copy use-attribute-sets="tag">
            <xsl:for-each select="*" >
              <xsl:if test="@inherited = '0'">
                <xsl:copy-of select="."/>
                <xsl:value-of select="$newline"/>
              </xsl:if>
            </xsl:for-each>
          </xsl:copy>
          <xsl:value-of select="$newline"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
    </metadata>
    <service>
	  <xsl:copy-of select="mycoreobject/service/*"/>
	</service>
  </mycoreobject>
</xsl:template>

</xsl:stylesheet>
