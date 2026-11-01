// Var nuevo al inicio de la clase
var onControlCenter: (() -> Unit)? = null
private var ccStartX = 0f
private var ccStartY = 0f
private var ccTracking = false

override fun dispatchTouchEvent(e: MotionEvent): Boolean {
    // Detectar swipe desde esquina superior derecha
    if (overlay == null && !ccTracking) {
        if (e.actionMasked == MotionEvent.ACTION_DOWN) {
            if (e.rawX > width * 0.7f && e.rawY < dp(40)) {
                ccTracking = true
                ccStartX = e.rawX
                ccStartY = e.rawY
                return true
            }
        }
    }
    if (ccTracking) {
        if (e.actionMasked == MotionEvent.ACTION_MOVE &&
            (e.rawY - ccStartY > dp(30) || ccStartX - e.rawX > dp(30))) {
            ccTracking = false
            onControlCenter?.invoke()
            return true
        }
        if (e.actionMasked == MotionEvent.ACTION_UP) ccTracking = false
        return true
    }

    // ... resto del código original (swipe unlock)
    return super.dispatchTouchEvent(e)
}
