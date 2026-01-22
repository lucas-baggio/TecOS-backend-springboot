package br.com.baggiotech.tecos_api.domain.workorder;

public enum OrderStatus {
    RECEBIDO,
    EM_ANALISE,
    AGUARDANDO_APROVACAO,
    EM_CONSERTO,
    PRONTO,
    ENTREGUE,
    CANCELADO;

    public static OrderStatus[] getAllowedNextStatuses(OrderStatus currentStatus) {
        return switch (currentStatus) {
            case RECEBIDO -> new OrderStatus[]{EM_ANALISE, CANCELADO};
            case EM_ANALISE -> new OrderStatus[]{AGUARDANDO_APROVACAO, CANCELADO};
            case AGUARDANDO_APROVACAO -> new OrderStatus[]{EM_CONSERTO, CANCELADO};
            case EM_CONSERTO -> new OrderStatus[]{PRONTO, CANCELADO};
            case PRONTO -> new OrderStatus[]{ENTREGUE, CANCELADO};
            case ENTREGUE, CANCELADO -> new OrderStatus[]{};
        };
    }

    public static boolean isStatusTransitionAllowed(OrderStatus from, OrderStatus to) {
        if (to == CANCELADO) {
            // Pode cancelar de qualquer status exceto ENTREGUE e CANCELADO
            return from != ENTREGUE && from != CANCELADO;
        }

        OrderStatus[] allowedNextStatuses = getAllowedNextStatuses(from);
        for (OrderStatus allowed : allowedNextStatuses) {
            if (allowed == to) {
                return true;
            }
        }
        return false;
    }
}
