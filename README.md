# JARVIS 8.0 — Android

Assistente de voz Android com palavra de ativação **Jarvis**.

### Melhorias
- Usa `Porcupine.BuiltInKeyword.JARVIS`.
- Para o detector antes de abrir o SpeechRecognizer e reinicia depois, evitando disputa pelo microfone.
- TTS em português do Brasil.
- Comandos locais: WhatsApp, Instagram, YouTube, configurações, pesquisa, alarme e telefone.
- Serviço em primeiro plano com tipo `microphone`.
- GitHub Actions para gerar o APK pelo celular.

### Configuração
O Porcupine exige uma AccessKey do Picovoice. O SDK Android documenta JARVIS como uma palavra integrada. Guarde a AccessKey em segredo.

1. Envie o projeto para um repositório GitHub.
2. Abra **Actions**.
3. Execute **Build JARVIS APK**.
4. Baixe o APK em **Artifacts**.
5. Instale no Android e permita o microfone.
6. Abra o JARVIS, coloque a AccessKey e toque em **ATIVAR JARVIS**.

O campo OpenAI permanece na interface para a próxima etapa de integração de IA; nesta versão ele ainda não faz uma chamada de API.

### Assistente do sistema
O Android documenta `VoiceInteractionService` como o serviço global do assistente selecionado pelo usuário e o mantém em execução para suportar hotwording. Uma versão futura pode migrar o JARVIS para essa arquitetura para uma integração mais profunda com o assistente padrão do aparelho.

### Limitações
Android e fabricantes podem limitar microfone em segundo plano e aplicar economia de bateria. Em alguns aparelhos será necessário retirar o JARVIS da otimização de bateria.
