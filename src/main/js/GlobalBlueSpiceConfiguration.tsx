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
import { ConfigurationForm, Title, Form } from "@scm-manager/ui-core";
import { HalRepresentation } from "@scm-manager/ui-types";
import { validation } from "@scm-manager/ui-components";

type GlobalBlueSpiceConfigurationDto = HalRepresentation & {
  baseUrl: string;
};

const GlobalBlueSpiceConfiguration: FC<{ link: string }> = ({ link }) => {
  const [t] = useTranslation("plugins");

  const isValidBaseUrl = (baseUrl: string) => {
    return baseUrl === "" || validation.isUrlValid(baseUrl);
  };

  return (
    <ConfigurationForm<GlobalBlueSpiceConfigurationDto>
      link={link}
      translationPath={["plugins", "scm-bluespice-plugin.config"]}
    >
      <Title>{t("scm-bluespice-plugin.config.title")}</Title>
      <p className="mb-2">{t("scm-bluespice-plugin.config.globalDescription")}</p>
      <Form.Row>
        <Form.Input name="baseUrl" rules={{ validate: isValidBaseUrl }} />
      </Form.Row>
    </ConfigurationForm>
  );
};

export default GlobalBlueSpiceConfiguration;
