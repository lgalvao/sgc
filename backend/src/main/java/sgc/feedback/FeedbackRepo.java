package sgc.feedback;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface FeedbackRepo extends JpaRepository<FeedbackRegistro, UUID> {
}
