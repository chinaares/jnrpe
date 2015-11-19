/*******************************************************************************
 * Copyright (c) 2007, 2014 Massimiliano Ziccardi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package it.jnrpe.plugin.jmx;

import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.plugins.IPluginInterface;
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricBuilder;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.plugins.PluginCommandLine;
import it.jnrpe.plugins.PluginDefinition;
import it.jnrpe.plugins.PluginOption;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.PluginRepositoryUtil;
import it.jnrpe.utils.thresholds.ReturnValueBuilder;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;

/**
 * The check JMX plugin.
 *
 * @author Massimiliano Ziccardi
 */
public class CCheckJMX extends JMXQuery implements IPluginInterface {

	/*
	 * Executes the plugin.
	 * 
	 * @param cl The command line
	 * 
	 * @return the result of the execution
	 * 
	 * @see it.jnrpe.plugins.PluginBase#execute(it.jnrpe.ICommandLine)
	 */
	@Override
	public ReturnValue execute(ICommandLine cl) throws BadThresholdException {
		try {
			ThresholdsEvaluatorBuilder thrb = new ThresholdsEvaluatorBuilder();
			configureThresholdEvaluatorBuilder(thrb, cl);

			connect();// JMXQuery
			execute();// JMXQuery

			ReturnValueBuilder builder = ReturnValueBuilder.forPlugin(
					getPluginName(), thrb.create());
			Collection<Metric> metrics = gatherMetrics(cl);

			for (Metric m : metrics) {
				if (m.getMetricName().equals(getCheckName())) {
					builder.withValue(m);
					break;
				}
			}
			ReturnValue ret = builder.create();
			for (Metric m : metrics) {
				if (!m.getMetricName().equals(getCheckName())) {
					ret.withPerformanceData(m, null, null, null);
				}
			}
			if (ret != null) {
				return ret;
			} else {
				return ReturnValueBuilder.forPlugin(this.getPluginName())
						.withForcedMessage("No metrics gathered")
						.withStatus(Status.UNKNOWN).create();
			}
		} catch (MetricGatheringException mge) {
			LOG.info(getContext(),
					"Plugin execution failed : " + mge.getMessage(), mge);
			return ReturnValueBuilder.forPlugin(getPluginName())
					.withForcedMessage(mge.getMessage())
					.withStatus(mge.getStatus()).create();
		} catch (Exception ex) {
			LOG.info(getContext(),
					"Plugin execution failed : " + ex.getMessage(), ex);
			return ReturnValueBuilder
					.forPlugin(getPluginName())
					.withForcedMessage(
							"An error has occurred during execution "
									+ "of the CHECK_JMX plugin : "
									+ ex.getMessage())
					.withStatus(Status.CRITICAL).create();
		} finally {
			try {
				disconnect();
			} catch (IOException e) {
				LOG.warn(
						getContext(),
						"An error has occurred during execution (disconnect) of the CHECK_JMX plugin : "
								+ e.getMessage(), e);
				// ByteArrayOutputStream bout = new ByteArrayOutputStream();
				// PrintStream ps = new PrintStream(bout);
				//
				// Status status = report(e, ps);
				// ps.flush();
				// ps.close();
				// return new ReturnValue(status, new
				// String(bout.toByteArray()));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.jnrpe.plugins.PluginBase#configureThresholdEvaluatorBuilder(it.jnrpe
	 * .utils.thresholds.ThresholdsEvaluatorBuilder, it.jnrpe.ICommandLine)
	 */
	@Override
	protected void configureThresholdEvaluatorBuilder(
			ThresholdsEvaluatorBuilder thrb, ICommandLine cl)
			throws BadThresholdException {

		if (cl.hasOption('U')) {
			setUrl(cl.getOptionValue('U'));
		}
		if (cl.hasOption('O')) {
			setObject(cl.getOptionValue('O'));
		}
		if (cl.hasOption('A')) {
			setAttribute(cl.getOptionValue('A'));
			setInfo_attribute(cl.getOptionValue('A'));
		}
		if (cl.hasOption('I')) {
			setInfo_attribute(cl.getOptionValue('I'));
		}
		if (cl.hasOption('J')) {
			setInfo_key(cl.getOptionValue('J'));
		}
		if (cl.hasOption('K')) {
			setAttribute_key(cl.getOptionValue('K'));
		}
		if (cl.hasOption('w')) {
			setWarning(cl.getOptionValue('w'));
		}
		if (cl.hasOption('c')) {
			setCritical(cl.getOptionValue('c'));
		}
		if (cl.hasOption("username")) {
			setUsername(cl.getOptionValue("username"));
		}
		if (cl.hasOption("password")) {
			setPassword(cl.getOptionValue("password"));
		}
		// if (cl.hasOption("string")) {
		// // WARNING if expected string not found (false = 0)
		// thrb.withLegacyThreshold("string", null, "0", null);
		// }
		// if (cl.hasOption("regex")) {
		// if (cl.hasOption("invert-regex")) {
		// // invert-regex: CRITICAL value if regex is found (true = 1)
		// thrb.withLegacyThreshold("invert-regex", null, null, "1");
		// } else {
		// // WARNING if regex not found (false = 0)
		// thrb.withLegacyThreshold("regex", null, null, "0");
		// }
		// }
		setVerbatim(2);
		// setVerbatim(4);

		thrb.withLegacyThreshold(this.getCheckName(), null, this.getWarning(),
				this.getCritical());

		super.configureThresholdEvaluatorBuilder(thrb, cl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.jnrpe.plugins.PluginBase#gatherMetrics(it.jnrpe.ICommandLine)
	 */
	@Override
	protected Collection<Metric> gatherMetrics(ICommandLine cl)
			throws MetricGatheringException {
		Collection<Metric> metrics = new ArrayList<Metric>(
				super.gatherMetrics(cl));
		if (this.getInfoData() == null || this.getVerbatim() >= 2) {
			String metricName = getCheckName();
			if (this.getCheckData() instanceof Number) {
				metrics.add(MetricBuilder
						.forMetric(metricName)
						.withMessage(metricName + " is {0}",
								this.getCheckData())
						.withValue((Number) this.getCheckData()).build());
			} else {
				metrics.add(MetricBuilder
						.forMetric(metricName)
						.withMessage(metricName + " is {0}",
								this.getCheckData()).withValue(0).build());
			}
			LOG.debug(getContext(), "Created metric : " + metricName);
		}
		if (this.getInfoData() != null) {
			String metricName = getInfo_attribute();
			if (this.getInfoData() instanceof CompositeDataSupport) {
				CompositeDataSupport data = (CompositeDataSupport) this
						.getInfoData();
				CompositeType type = data.getCompositeType();
				for (Iterator<String> it = type.keySet().iterator(); it
						.hasNext();) {
					String key = (String) it.next();
					if (data.containsKey(key)
							&& !getCheckName().equals(metricName + "." + key)) {
						if (data.get(key) instanceof Number) {
							metrics.add(MetricBuilder
									.forMetric(metricName + "." + key)
									.withMessage(
											metricName + "." + key + " is {0}",
											data.get(key))
									.withValue((Number) data.get(key)).build());
						} else {
							metrics.add(MetricBuilder
									.forMetric(metricName + "." + key)
									.withMessage(
											metricName + "." + key + " is {0}",
											data.get(key)).withValue(0).build());
						}
						LOG.debug(getContext(), "Created metric : "
								+ metricName + "." + key);
					}
				}
			} else {
				if (this.getInfoData() instanceof Number) {
					metrics.add(MetricBuilder
							.forMetric(metricName)
							.withMessage("info is {0}",
									this.getCheckData().toString())
							.withValue((Number) this.getInfoData()).build());
				} else {
					metrics.add(MetricBuilder
							.forMetric(metricName)
							.withMessage("info is {0}",
									this.getCheckData().toString())
							.withValue(0).build());
				}
				LOG.debug(getContext(), "Created metric : " + "info");
			}
		}

		return metrics;
	}

	@Override
	protected String getPluginName() {
		return "CHECK_JMX";
	}

	protected String getCheckName() {
		String s = this.getAttribute();
		if (this.getAttribute_key() != null) {
			s = this.getAttribute() + '.' + this.getAttribute_key();
		}
		return s;
	}

	protected String getInfoName() {
		String s = this.getInfo_attribute();
		if (this.getInfo_key() != null) {
			s = this.getInfo_attribute() + '.' + this.getInfo_key();
		}
		return s;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CCheckJMX checkJMX = new CCheckJMX();
		Status status;
		try {
			// query.parse(args);
			// configure a parser
			PluginDefinition pluginDef = PluginRepositoryUtil
					.parseXmlPluginDefinition(
							JMXQuery.class.getClassLoader(),
							CCheckJMX.class
									.getResourceAsStream("/check_jmx_plugin.xml"));
			GroupBuilder gBuilder = new GroupBuilder();
			for (PluginOption po : pluginDef.getOptions()) {
				gBuilder = gBuilder.withOption(po.toOption());
			}
			HelpFormatter hf = new HelpFormatter();
			Parser cliParser = new Parser();
			cliParser.setGroup(gBuilder.create());
			cliParser.setHelpFormatter(hf);
			CommandLine cl = cliParser.parse(args);
			ReturnValue retValue = checkJMX.execute(new PluginCommandLine(cl));
			status = retValue.getStatus();
			System.out.println(retValue.getMessage());
		} catch (Exception ex) {
			status = checkJMX.report(ex, System.out);
		} finally {
			try {
				checkJMX.disconnect();
			} catch (IOException e) {
				status = checkJMX.report(e, System.out);
			}
		}
		System.exit(status.intValue());
	}
}
