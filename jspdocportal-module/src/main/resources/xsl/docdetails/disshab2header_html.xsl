<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:mcrmods="xalan://org.mycore.mods.classification.MCRMODSClassificationSupport" exclude-result-prefixes="mods mcrmods">

    <!-- to enable relative urls in import set xsltSystemId attribute in x:transform of JSP XML Tag Library !!! -->
	<xsl:import href="mods-util.xsl" />
	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

	<xsl:param name="WebApplicationBaseURL"></xsl:param>

	<xsl:template match="/">
		<xsl:for-each select="/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods">

				<p>
					<xsl:for-each select="./mods:name[@type='personal'][position()=1 or mods:role/mods:roleTerm[@type='code']/@valueURI='http://id.loc.gov/vocabulary/relators/aut']">
						<xsl:call-template name="display-name">
							<xsl:with-param name="name" select="." />
						</xsl:call-template>
					</xsl:for-each>
				</p>
				<xsl:call-template name="mods-title" />
                <p>
					<xsl:variable name="classlink" select="mcrmods:getClassCategLink(mods:classification[@displayLabel='doctype'])" />
					<xsl:if test="string-length($classlink) &gt; 0">
						<xsl:value-of select="concat(document($classlink)/mycoreclass/categories/category/label[@xml:lang='de']/@text, ', ')" />
						</xsl:if>
					<xsl:call-template name="mods-originInfo" />
				</p>

				<xsl:if test="./mods:abstract">
					<p class="ir-docdetails-abstract">
						<xsl:value-of select="./mods:abstract" />
					</p>
					<p>&#160;</p>
				</xsl:if>
				<xsl:if test="./mods:identifier[@type='doi']">
					<p>
						DOI:&#160;
						<xsl:element name="a">
							<xsl:attribute name="href">http://dx.doi.org/<xsl:value-of select="./mods:identifier[@type='doi']" /></xsl:attribute>
							<xsl:value-of select="./mods:identifier[@type='doi']" />
						</xsl:element>
					</p>
				</xsl:if>
				<xsl:if test="./mods:identifier[@type='purl']">
					<p>
						PURL:&#160;
						<xsl:element name="a">
							<xsl:attribute name="href"><xsl:value-of select="./mods:identifier[@type='purl']" /></xsl:attribute>
							<xsl:value-of select="./mods:identifier[@type='purl']" />
						</xsl:element>
					</p>
				</xsl:if>
		</xsl:for-each>

	</xsl:template>

</xsl:stylesheet>