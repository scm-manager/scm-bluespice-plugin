/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationForm, Subtitle, Form } from "@scm-manager/ui-core";
import { HalRepresentation } from "@scm-manager/ui-types";
import { BaseUrlWrapper, LinkButton } from "./BlueSpiceRepositoryUtil";
import { validation } from "@scm-manager/ui-components";

type BlueSpiceRepositoryConfigurationDto = HalRepresentation & {
  directUrl: string;
  relativePath: string;
  override: string;
  _links: {
    baseUrl?: {
      href: string;
    };
  };
};

const BlueSpiceRepositoryConfiguration: FC<{ link: string }> = ({ link }) => {
  const [t] = useTranslation("plugins");
  const handleLink = (route: string) => {
    window.open(route, "_blank", "noreferrer");
  };

  const handleLinkWithBaseUrl = (baseUrl: string, route: string) => {
    if (route) {
      if (!baseUrl.endsWith("/") && !route.startsWith("/")) {
        handleLink(baseUrl + "/" + route);
      } else if (baseUrl.endsWith("/") && route.startsWith("/")) {
        handleLink(baseUrl + route.replace("/", ""));
      } else {
        handleLink(baseUrl + route);
      }
    } else {
      handleLink(baseUrl);
    }
  };

  const isValidUrl = (value: string) => {
    return validation.isUrlValid(value);
  };

  return (
    <ConfigurationForm<BlueSpiceRepositoryConfigurationDto>
      link={link}
      translationPath={["plugins", "scm-bluespice-plugin.config"]}
    >
      {({ watch }) => {
        return (
          <>
            <Subtitle>{t("scm-bluespice-plugin.config.title")}</Subtitle>
            {watch("_links.baseUrl")?.href ? (
              <>
                <p className="mb-4">{t("scm-bluespice-plugin.config.baseUrl.header")}</p>
                <BaseUrlWrapper>{watch("_links.baseUrl")?.href}</BaseUrlWrapper>
                <p className="mt-4">{t("scm-bluespice-plugin.config.baseUrl.description")}</p>
                <p className="label is-size-6 mt-5">{t("scm-bluespice-plugin.config.configure")}</p>
                <Form.RadioGroup name="override">
                  <Form.RadioGroup.Option value="APPEND" label={t("scm-bluespice-plugin.config.appendPath.option")} />
                  <Form.RadioGroup.Option
                    value="OVERRIDE"
                    label={t("scm-bluespice-plugin.config.overridePath.option")}
                  />
                </Form.RadioGroup>
                <p className="mt-4 has-text-weight-bold" hidden={watch("override") === "OVERRIDE"}>
                  {t("scm-bluespice-plugin.config.appendPath.label")}
                </p>
                <Form.Row hidden={watch("override") === "OVERRIDE"}>
                  <div className="field column has-addons">
                    <Form.Input name="relativePath" className="control p-0" />
                    <LinkButton className="control mb-3">
                      <button
                        className="button is-info"
                        onClick={() =>
                          handleLinkWithBaseUrl(watch("_links.baseUrl")?.href ?? "", watch("relativePath"))
                        }
                      >
                        {t("scm-bluespice-plugin.config.button")}
                      </button>
                    </LinkButton>
                  </div>
                </Form.Row>
                <p className="mt-4 has-text-weight-bold" hidden={watch("override") === "APPEND"}>
                  {t("scm-bluespice-plugin.config.overridePath.label")}
                </p>
                <Form.Row hidden={watch("override") === "APPEND"}>
                  <div className="field column has-addons">
                    <Form.Input name="directUrl" className="control p-0" rules={{ validate: isValidUrl }} />
                    <LinkButton className="control mb-3">
                      <button className="button is-info" onClick={() => handleLink(watch("directUrl"))}>
                        {t("scm-bluespice-plugin.config.button")}
                      </button>
                    </LinkButton>
                  </div>
                </Form.Row>
              </>
            ) : (
              <>
                <p className="mt-6 mb-4">{t("scm-bluespice-plugin.config.overridePath.header")}</p>
                <p className="label">{t("scm-bluespice-plugin.config.overridePath.label")}</p>
                <Form.Row>
                  <div className="field column has-addons">
                    <Form.Input name="directUrl" className="control p-0" rules={{ validate: isValidUrl }} />
                    <LinkButton className="control mb-3">
                      <button className="button is-info" onClick={() => handleLink(watch("directUrl"))}>
                        {t("scm-bluespice-plugin.config.button")}
                      </button>
                    </LinkButton>
                  </div>
                </Form.Row>
              </>
            )}
          </>
        );
      }}
    </ConfigurationForm>
  );
};

export default BlueSpiceRepositoryConfiguration;
