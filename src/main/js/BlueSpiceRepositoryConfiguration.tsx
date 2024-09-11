/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
    if (!route) {
      handleLink(baseUrl);
      return;
    }

    if (route.startsWith("/")) {
      route = route.substring(1, route.length);
    }

    handleLink(`${baseUrl}/${route}`);
  };

  const isValidUrl = (value: string) => {
    return value === "" || validation.isUrlValid(value);
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
